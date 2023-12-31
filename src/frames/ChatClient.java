package frames;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

import java.util.HashMap;


public class ChatClient extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
        
        
        setTitle("4-GTR Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        
        ImageIcon icon = new ImageIcon("C:\\Users\\ABDESSAMAD EL OIDII\\eclipse-workspace\\Chat_Application\\src\\imgs\\chaticon.png");
        setIconImage(icon.getImage());

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        messageField = new PlaceholderTextField("message");
        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(new Color(52, 152, 219));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
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
        sendFileButton.setForeground(Color.WHITE);
        sendFileButton.setBackground(new Color(52, 152, 219));
        sendFileButton.setFocusPainted(false);
        sendFileButton.setBorderPainted(false);
        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseAndSendFile();
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(sendFileButton,BorderLayout.WEST);
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
                JOptionPane.showMessageDialog(this, "Incorrect password. Please try again or cancel to exit.", "Error", JOptionPane.ERROR_MESSAGE);
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
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                        	while (inputStream.hasNextLine()) {
                                String message = inputStream.nextLine();

                                if (message.startsWith("/newUser")) {
                                    String newUser = message.substring(8);
                                    displaySystemMessage(newUser + " has joined the chat.");
                                } else {
                                    displayMessage(message);
                                }
                            }
                        }
                    }).start();
                    outputStream.println("new User has joined the chat : " + nickname);
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

            displayStyledText(chatArea, userStyle, sender + ": ");
            displayStyledText(chatArea, null, content + "\n");
        }
    }

    private SimpleAttributeSet createUserStyle(String username) {
        SimpleAttributeSet userStyle = new SimpleAttributeSet();
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

        if (!message.isEmpty() && !message.equalsIgnoreCase("message")) {
            outputStream.println(message);
            messageField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid message. Please enter a valid message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void chooseAndSendFile() {
        JTextField recipientField = new JTextField();
        Object[] message = {
                "Recipient's Nickname:", recipientField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Enter Recipient", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String recipientNickname = recipientField.getText().trim();
            if (!recipientNickname.isEmpty()) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    sendFile(selectedFile, recipientNickname);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Recipient's nickname cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void sendFile(File file, String recipientNickname) {
        outputStream.println("/file");
        outputStream.println(file.getName());
        outputStream.println(recipientNickname);
        int bytes = 0;
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }
}
