package chat_badr;

//ClientHandler.java

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileOutputStream;

public class ClientHandler implements Runnable {
 private static final String FILE_SIGNAL = "/file";

 private DataInputStream dataInputStream = null;

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
         this.dataInputStream = new DataInputStream(socket.getInputStream());
         this.nickname = nickname;
         this.usedNicknames = usedNicknames;
     } catch (IOException e) {
         e.printStackTrace();
     }
 }

 @Override
 public void run() {
     try {
         sendMessage("\nWelcome to the chat!");

         for (ClientHandler client : clients) {
             if (client != this) {
                 sendMessage("User " + client.getNickname() + " is already in the chat.");
             }
         }

         while (inputStream.hasNextLine()) {
             String message = inputStream.nextLine();

             if (message.equals(FILE_SIGNAL)) {
                 try {
                     String nameOfFile = inputStream.nextLine();
                     System.out.println("Creating the file !!");
                     System.out.println(nameOfFile);

                     String destinationDirectory = chooseDirectory();

                     if (destinationDirectory != null) {
                         receiveFile(nameOfFile, destinationDirectory, dataInputStream);
                     } else {
                         System.out.println("File transfer cancelled by user.");
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             } else if (message.startsWith("/msg")) {
                 String[] parts = message.split(" ", 3);
                 if (parts.length == 3) {
                     String targetUser = parts[1];
                     String privateMessage = parts[2];
                     sendPrivateMessage(targetUser, privateMessage);
                 }
             }else {
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

 private static void receiveFile(String fileName, String destinationDirectory, DataInputStream dataInputStream) throws Exception {
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
 }

 private synchronized void broadcast(String message) {
     for (ClientHandler client : clients) {
         client.sendMessage(message);
     }
 }

 private void sendPrivateMessage(String targetUser, String message) {
     for (ClientHandler client : clients) {
         if (client.getNickname().equals(targetUser)) {
             client.sendMessage("[Private from " + nickname + "]: " + message);
             break;
         }
     }
 }

 void sendMessage(String message) {
     outputStream.println(message);
 }

 public String getNickname() {
     return nickname;
 }
}
