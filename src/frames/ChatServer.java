package frames;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class ChatServer {
    private static final int PORT = 8888;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static List<String> usedNicknames = new ArrayList<>(); // Add this line

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection: " + clientSocket);
                
                PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

                // Prompt user for a nickname
                String nickname = new java.util.Scanner(clientSocket.getInputStream()).nextLine();
                System.out.println("/waiting");

                // Check if the nickname is already used
                if (!usedNicknames.contains(nickname)) {
                    usedNicknames.add(nickname);

                    ClientHandler clientHandler = new ClientHandler(clientSocket, clients, nickname, usedNicknames);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                    outputStream.println("/OK");
                    System.out.println("/added");
                } else {
                    // Send a message to the client that the nickname is not valid
                    System.out.println("/verifyNickname ERROR");
                    outputStream.println("/verifyNickname ERROR");
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
