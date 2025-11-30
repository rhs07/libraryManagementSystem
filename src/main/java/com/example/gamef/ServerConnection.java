package com.example.gamef;

import java.io.*;
import java.net.Socket;

/**
 * Handles client-side socket connection for JavaFX app
 */
public class ServerConnection {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;

    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    public static void connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(HOST, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("✅ Connected to server!");

                // Listen for messages from server
                new Thread(ServerConnection::listenForMessages).start();
            }
        } catch (IOException e) {
            System.out.println("❌ Could not connect to server!");
            e.printStackTrace();
        }
    }

    public static String send(String message) {
        try {
            connect();
            out.println(message);
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    private static void listenForMessages() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("📡 Server message: " + msg);
                if (msg.startsWith("ISSUE_REQUEST:")) {
                    javafx.application.Platform.runLater(() ->
                            System.out.println("🔔 New Issue Request received in Librarian App!"));
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Server connection lost.");
        }
    }
}
