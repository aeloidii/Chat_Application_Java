package chat;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ChatClient extends JFrame {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;
    private static final String SERVER_PASSWORD = "chatappjava";
    private static DataOutputStream dataOutputStream = null;

    private Socket socket;
    private PrintWriter outputStream;
    private Scanner inputStream;

    private JTextPane chatArea;
    private JTextField messageField;
    private String nickname;
    private JButton sendFileButton;
    private SimpleAttributeSet systemStyle;
    private Map<String, SimpleAttributeSet> userStyles;

    public ChatClient() {
        authenticateUser();

        // Initialize styles
        systemStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(systemStyle, Color.BLUE);

        userStyles = new HashMap<>();

        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        ImageIcon icon = new ImageIcon("C:\\Users\\ABDESSAMAD EL OIDII\\eclipse-workspace\\Chat_Application\\src\\imgs\\chaticon.png");
        setIconImage(icon.getImage());

        // Set the background color of chatArea to black
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(0x121212));  // Set background color of chatArea
        chatArea.setPreferredSize(new Dimension(500, chatArea.getPreferredSize().height));

	     // Add some spacing between bubble messages
	    chatArea.setMargin(new Insets(10, 10, 10, 10));
	
	    JScrollPane scrollPane = new JScrollPane(chatArea);
	    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        int scrollUnitIncrement = 15;
        int scrollBlockIncrement = 50;

        scrollPane.getVerticalScrollBar().setUnitIncrement(scrollUnitIncrement);
        scrollPane.getVerticalScrollBar().setBlockIncrement(scrollBlockIncrement);
        
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());


        add(scrollPane, BorderLayout.CENTER);

        messageField = new PlaceholderTextField("message");
        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setForeground(Color.black);
        sendButton.setBackground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.addActionListener(e -> sendMessage());
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        sendFileButton = new JButton("Send File");
        sendFileButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendFileButton.setForeground(Color.black);
        sendFileButton.setBackground(Color.WHITE);
        sendFileButton.setFocusPainted(false);
        sendFileButton.setBorderPainted(false);
        sendFileButton.addActionListener(e -> chooseAndSendFile());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(sendFileButton, BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);

        connectToServer();

        setVisible(true);
    }

    private void authenticateUser() {
        boolean authenticated = false;

        while (!authenticated) {
            String password = JOptionPane.showInputDialog(this, "Enter the password to join the chat:");

            if (password != null && password.equals(SERVER_PASSWORD)) {
                NicknameDialog nicknameDialog = new NicknameDialog(this);
                nicknameDialog.setVisible(true);

                if (!nicknameDialog.isNicknameEntered()) {
                    System.exit(0);
                }

                this.nickname = nicknameDialog.getNickname();
                authenticated = true;
            } else {
                int option = JOptionPane.showConfirmDialog(this, "Incorrect password. Try in different time :)", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        }
    }


    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new Scanner(socket.getInputStream());

            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            outputStream.println(nickname);

            if (inputStream.hasNextLine()) {
                String verificationResponse = inputStream.nextLine();

                if ("/verifyNickname ERROR".equals(verificationResponse)) {
                    JOptionPane.showMessageDialog(this, "The chosen nickname is not valid. Please choose a different one.", "Error", JOptionPane.ERROR_MESSAGE);
                    while (true) {
                        this.nickname = JOptionPane.showInputDialog(this, "Enter your nickname:");
                        if (!nickname.trim().isEmpty()) {
                            outputStream.println(nickname);
                            break;
                        } else {
                            JOptionPane.showMessageDialog(this, "Nickname cannot be empty. Please choose a different one.");
                        }
                    }

                    connectToServer();
                } else if ("/OK".equals(verificationResponse)) {
                    new Thread(() -> {
                        while (inputStream.hasNextLine()) {
                            String message = inputStream.nextLine();

                            if (message.startsWith("/newUser")) {
                                String newUser = message.substring(8);
                                displaySystemMessage(newUser + " has joined the chat.");
                            } else {
                                displayMessage(message);
                            }
                        }
                    }).start();

                    outputStream.println(" new User has joined the chat : " + nickname);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Server did not respond. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(String message) {
        String[] parts = message.split(": ", 2);
        if (parts.length == 2) {
            String sender = parts[0];
            String content = parts[1];

            SimpleAttributeSet userStyle = userStyles.computeIfAbsent(sender, this::createUserStyle);

            JPanel messagePanel = createMessagePanel(sender, content, userStyle);
            addMessagePanelToChatArea(messagePanel);
        } else if (message.startsWith("(Private)")) {
            displayPrivateMessage(message);
        } else {
            displayStyledText(chatArea, systemStyle, message + "\n");
        }
    }

    private void displayPrivateMessage(String message) {
        String[] parts = message.split(": ", 2);
        if (parts.length == 2) {
            String sender = parts[0].substring("(Private) ".length());
            String content = parts[1];

            SimpleAttributeSet userStyle = userStyles.computeIfAbsent(sender, this::createUserStyle);

            JPanel messagePanel = createMessagePanel(sender, content, userStyle);
            addMessagePanelToChatArea(messagePanel);
        }
    }

    private void addMessagePanelToChatArea(JPanel messagePanel) {
        StyledDocument doc = chatArea.getStyledDocument();

        // Insert a newline before the message
        try {
            doc.insertString(doc.getLength(), "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Insert the message panel as an element
        chatArea.insertComponent(messagePanel);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        chatArea.repaint();
    }


    private JPanel createMessagePanel(String sender, String content, SimpleAttributeSet userStyle) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));  // Grey border
        messagePanel.setBackground(new Color(220, 220, 220));  // Light grey background

        JLabel senderLabel = new JLabel(sender + ": ");
        senderLabel.setForeground(StyleConstants.getForeground(userStyle));

        JTextArea contentArea = new JTextArea(content);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setForeground(Color.BLACK);  // Set text color to black
        contentArea.setBackground(new Color(220, 220, 220));  // Match the background color of the message panel

        messagePanel.add(senderLabel, BorderLayout.WEST);
        messagePanel.add(contentArea, BorderLayout.CENTER);

        return messagePanel;
    }



    private SimpleAttributeSet createUserStyle(String username) {
        SimpleAttributeSet userStyle = new SimpleAttributeSet();
        
        
        Font font = new Font("Arial", Font.PLAIN, 12);
        StyleConstants.setFontFamily(userStyle, font.getFamily());
        StyleConstants.setFontSize(userStyle, font.getSize());
        StyleConstants.setBold(userStyle, font.isBold());
        StyleConstants.setItalic(userStyle, font.isItalic());

        
        StyleConstants.setForeground(userStyle, getRandomColor());

        return userStyle;
    }


    private void displaySystemMessage(String message) {
        displayStyledText(chatArea, systemStyle, message + "\n");
    }

    private void displayStyledText(JTextPane textPane, AttributeSet style, String text) {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private Color getRandomColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        return new Color(r, g, b);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();

        if (!message.isEmpty()) {
            if (message.startsWith("/msg ")) {
                String[] parts = message.split(" ", 3);
                if (parts.length == 3) {
                    String targetUser = parts[1];
                    String privateMessage = parts[2];
                    sendPrivateMessage(targetUser, privateMessage);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid private message format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                outputStream.println(message);
            }
            messageField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid message. Please enter a valid message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void sendPrivateMessage(String targetUser, String message) {
        outputStream.println("/msg " + targetUser + " " + message);
    }

    

    private void chooseAndSendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            sendFile(selectedFile);
        }
    }

    private void sendFile(File file) {
        outputStream.println(file.getName());

        outputStream.println("/file");
        outputStream.println(file.getName());
        int bytes = 0;
        System.out.println(file);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);

            dataOutputStream.writeLong(file.length());
            System.out.println("size to create : " + file.length());

            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }
            System.out.println("the file has been sent.");
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}
