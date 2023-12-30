package frames;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JFileChooser;

public class ClientHandler implements Runnable {
    private DataInputStream dataInputStream = null;

    private Socket clientSocket;
    private List<ClientHandler> clients;
    private PrintWriter outputStream;
    private Scanner inputStream;
    private String nickname;
    private static Map<String, ClientHandler> nicknameToClientHandlerMap = new HashMap<>();

    public ClientHandler(Socket socket, List<ClientHandler> clients, String nickname) {
        this.clientSocket = socket;
        this.clients = clients;

        try {
            this.outputStream = new PrintWriter(socket.getOutputStream(), true);
            this.inputStream = new Scanner(socket.getInputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.nickname = nickname;
            
            nicknameToClientHandlerMap.put(nickname, this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
        	broadcast(nickname + " has joined the chat.");
            while (inputStream.hasNextLine()) {
                String message = inputStream.nextLine();

                if (message.startsWith("/private")) {
                    String[] parts = message.split(" ", 3);
                    String recipientNickname = parts[1];
                    String privateMessage = parts[2];

                    if (nicknameToClientHandlerMap.containsKey(recipientNickname)) {
                        ClientHandler recipientHandler = nicknameToClientHandlerMap.get(recipientNickname);
                        recipientHandler.sendMessage("(Private) " + nickname + ": " + privateMessage);
                        sendMessage("(Private) to " + recipientNickname + ": " + privateMessage);
                    } else {
                        sendMessage("User " + recipientNickname + " not found or offline.");
                    }
                }
                else if (message.equals("/file")) {
                    try {
                        String nameOfFile = inputStream.nextLine();
                        System.out.println("Creating the file !!");
                        System.out.println(nameOfFile);

                        String recipientNickname = inputStream.nextLine();
                        System.out.println(recipientNickname);
                        if (nicknameToClientHandlerMap.containsKey(recipientNickname)) {
                               ClientHandler recipientHandler = nicknameToClientHandlerMap.get(recipientNickname);
                               recipientHandler.sendMessage("(Private) " + nickname + ": " + nameOfFile);
                               receiveFile(nameOfFile);
                        } else {
                        		sendMessage("User " + recipientNickname + " not found or offline.");
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    broadcast(nickname + ": " + message);
                }
            }
        } finally {
            try {
                clientSocket.close();
                clients.remove(this);
                broadcast(nickname + " has left the chat.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String chooseDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Choose Destination Directory");

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            return selectedDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

    private void receiveFile(String fileName) {
        try {
            System.out.println("Creating the file !!");
            System.out.println(fileName);
            
            String destinationDirectory = chooseDirectory();

            if (destinationDirectory != null) {
                System.out.println("The file is processed in: " + destinationDirectory);
                int bytes = 0;
                FileOutputStream fileOutputStream = new FileOutputStream(destinationDirectory + File.separator + fileName);

                long size = dataInputStream.readLong();
                System.out.println("Size to create: " + size);
                byte[] buffer = new byte[4 * 1024];
                while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes;
                }
                System.out.println("The file has been created in " + destinationDirectory);
                fileOutputStream.close();
            } else {
                System.out.println("File transfer cancelled by user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private void sendMessage(String message) {
        outputStream.println(message);
    }
}
