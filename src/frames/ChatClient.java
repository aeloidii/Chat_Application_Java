package frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class ChatClient extends JFrame {
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
        // Prompt user for a nickname
        while (true) {
            this.nickname = JOptionPane.showInputDialog(this, "Enter your nickname:");
            if (isValidNickname(nickname)) {
                break;
            } else {
                JOptionPane.showMessageDialog(this, "Invalid nickname. Please choose a different one.");
            }
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

        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        sendFileButton = new JButton("Send File");
        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseAndSendFile();
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendFileButton);

        add(buttonPanel, BorderLayout.NORTH);

        connectToServer();

        setVisible(true);
    }

    private boolean isValidNickname(String nickname) {
        return !nickname.trim().isEmpty();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new Scanner(socket.getInputStream());
            
            dataOutputStream = new DataOutputStream(socket.getOutputStream());


            // Send the chosen nickname to the server
            outputStream.println(nickname);

            // Start the thread to listen for incoming messages
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (inputStream.hasNextLine()) {
                        String message = inputStream.nextLine();
                        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        outputStream.println(nickname + ": " + message);
        messageField.setText("");
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
