package com.example.server;

import com.example.gamef.DatabaseConnection;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClientHandler extends Thread {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String role;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("📩 " + message);
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("⚠ Client lost connection");
        } finally {
            Server.removeClient(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleMessage(String msg) {
        try {
            if (msg.startsWith("LOGIN:")) handleLogin(msg.substring(6));
            else if (msg.startsWith("REGISTER:")) handleRegister(msg.substring(9));
            else if (msg.startsWith("ISSUE_REQUEST:")) {
                // 🟢 Notify all librarians
                Server.broadcastToRole("Librarian", msg);
                out.println("REQUEST_SENT");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    private void handleLogin(String data) {
        String[] parts = data.split(",");
        if (parts.length < 2) return;
        String user = parts[0];
        String pass = parts[1];

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT role FROM user_account WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                this.username = user;
                this.role = rs.getString("role");
                out.println("SUCCESS:" + role);
                System.out.println("✅ " + user + " logged in as " + role);
            } else {
                out.println("FAIL");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    private void handleRegister(String data) {
        String[] parts = data.split(",");
        if (parts.length < 5) {
            out.println("ERROR");
            return;
        }

        String firstname = parts[0];
        String lastname = parts[1];
        String username = parts[2];
        String password = parts[3];
        String role = parts[4];

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkQuery = "SELECT * FROM user_account WHERE username=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                out.println("EXISTS");
                return;
            }

            String insertQuery = "INSERT INTO user_account (firstname, lastname, username, password, role) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertQuery);
            ps.setString(1, firstname);
            ps.setString(2, lastname);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.setString(5, role);
            ps.executeUpdate();

            out.println("REGISTERED");
            System.out.println("🟢 New user: " + username + " (" + role + ")");
        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }
}
