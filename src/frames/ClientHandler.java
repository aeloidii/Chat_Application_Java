package frames;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ClientHandler implements Runnable {
    private static final String FILE_SIGNAL = "/file";

    private static DataInputStream dataInputStream = null;

    private Socket clientSocket;
    private List<ClientHandler> clients;
    private PrintWriter outputStream;
    private Scanner inputStream;
    private String nickname;
    private List<String> usedNicknames;

    public ClientHandler(Socket socket, List<ClientHandler> clients, String nickname, List<String> usedNicknames) {
        this.clientSocket = socket;
        this.clients = clients;

        try {
            this.outputStream = new PrintWriter(socket.getOutputStream(), true);
            this.inputStream = new Scanner(socket.getInputStream());
            ClientHandler.dataInputStream = new DataInputStream(socket.getInputStream());
            this.nickname = nickname;
            this.usedNicknames = usedNicknames;

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

                if (message.equals(FILE_SIGNAL)) {
                    try {
                        String nameOfFile = inputStream.nextLine();
                        System.out.println("Creating the file !!");
                        System.out.println(nameOfFile);

                        // Ask the user for the destination directory using JavaFX DirectoryChooser
                        String destinationDirectory = chooseDirectory();

                        if (destinationDirectory != null) {
                            receiveFile(nameOfFile, destinationDirectory);
                        } else {
                            System.out.println("File transfer cancelled by user.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
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
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Choose Destination Directory");

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            return selectedDirectory.getAbsolutePath();
        } else {
            return null; // User canceled the operation
        }
    }

    private static void receiveFile(String fileName, String destinationDirectory) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(destinationDirectory + File.separator + fileName);

        long size = dataInputStream.readLong();    // read file size
        System.out.println("size to create 1 : " + size);
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;      // read up to file size
        }
        System.out.println("The file has been created in " + destinationDirectory);
        fileOutputStream.close();
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
