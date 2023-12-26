package frames;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private List<ClientHandler> clients;
    private PrintWriter outputStream;
    private String nickname;

    public ClientHandler(Socket socket, List<ClientHandler> clients, String nickname) {
        this.clientSocket = socket;
        this.clients = clients;

        try {
            this.outputStream = new PrintWriter(socket.getOutputStream(), true);
            this.nickname = nickname;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Scanner inputStream = new Scanner(clientSocket.getInputStream());

            while (inputStream.hasNextLine()) {
                String message = inputStream.nextLine();
                broadcast(nickname + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private void sendMessage(String message) {
        outputStream.println(message);
    }
}
