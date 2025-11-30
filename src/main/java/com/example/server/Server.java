package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 1234;
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 New client connected: " + clientSocket);
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Server error: " + e.getMessage());
        }
    }

    // ✅ Send message to all users of a specific role (e.g., Librarian)
    public static void broadcastToRole(String role, String message) {
        for (ClientHandler client : clients) {
            if (client.getRole() != null && client.getRole().equalsIgnoreCase(role)) {
                client.sendMessage(message);
            }
        }
    }

    // ✅ Remove disconnected client
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("❌ Client disconnected: " + client.getUsername());
    }
}
