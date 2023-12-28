package frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatClient extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;
    private static DataOutputStream dataOutputStream = null;

    private Socket socket;
    private PrintWriter outputStream;
    private Scanner inputStream;

    private JTextArea chatArea;
    private JTextField messageField;
    private String nickname;
    private JButton sendFileButton;
    
    

    public ChatClient() {
    	while (true) {
            this.nickname = JOptionPane.showInputDialog(this, "Enter your nickname:");
            if (!nickname.trim().isEmpty()) {
                    break;
            }
            else JOptionPane.showMessageDialog(this, "Already exists. Please choose a different one.");
        }
    	
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        messageField = new PlaceholderTextField("message");
        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(new Color(52, 152, 219)); // Use a cool color
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
        sendFileButton.setBackground(new Color(52, 152, 219)); // Use a cool color
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

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new Scanner(socket.getInputStream());

            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            // Send the chosen nickname to the server
            outputStream.println(nickname);

            // Wait for the server's response
            if (inputStream.hasNextLine()) {
                String verificationResponse = inputStream.nextLine();

                if ("/verifyNickname ERROR".equals(verificationResponse)) {
                    // Nickname is not valid, display an error message and prompt for a new nickname
                    JOptionPane.showMessageDialog(this, "The chosen nickname is not valid. Please choose a different one.", "Error", JOptionPane.ERROR_MESSAGE);

                    // Keep prompting for a new nickname until a valid one is entered
                    while (true) {
                        this.nickname = JOptionPane.showInputDialog(this, "Enter your nickname:");
                        if (!nickname.trim().isEmpty()) {
                            outputStream.println(nickname); // Send the new nickname to the server
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
                                SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
                            }
                        }
                    }).start();
                }
            } else {
                // Handle the case where the server did not send any response
                JOptionPane.showMessageDialog(this, "Server did not respond. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
	            
	            // send file size
	            dataOutputStream.writeLong(file.length());
	            System.out.println("size to create : "+ file.length());
	            // break file into chunks
	            byte[] buffer = new byte[4*1024];
	            while ((bytes=fileInputStream.read(buffer))!=-1){
	                dataOutputStream.write(buffer,0,bytes);
	                dataOutputStream.flush();
	            }
	            System.out.println("the file has been sent.");
	            fileInputStream.close();
            }catch(Exception e) {
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
