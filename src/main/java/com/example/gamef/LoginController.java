package com.example.gamef;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button goToRegisterButton;

    @FXML
    private Button CancelButton;

    // 🔹 LOGIN
    @FXML
    void loginButtonOnAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in all fields!");
            return;
        }

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {

                String query = "SELECT firstname, lastname, role FROM user_account WHERE username=? AND password=?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String role = rs.getString("role");
                    String fullName = rs.getString("firstname") + " " + rs.getString("lastname");
                    String memberId = "N/A";

                    // ✅ যদি Student হয়, তাহলে members table থেকে তার member id আনো
                    if (role.equalsIgnoreCase("Student")) {
                        String memberQuery = "SELECT id FROM members WHERE username=?";
                        PreparedStatement psMember = conn.prepareStatement(memberQuery);
                        psMember.setString(1, username);
                        ResultSet rsMember = psMember.executeQuery();
                        if (rsMember.next()) {
                            memberId = rsMember.getString("id");
                        }
                    }

                    // 🔸 Create user session with correct data
                    UserSession.createSession(username, role, memberId, fullName);

                    javafx.application.Platform.runLater(() -> {
                        if (role.equalsIgnoreCase("Student")) {
                            goToPage("student_home.fxml", "Student Dashboard");
                        } else if (role.equalsIgnoreCase("Librarian")) {
                            goToPage("home.fxml", "Librarian Dashboard");
                        } else {
                            showAlert("Error", "Unknown role in database: " + role);
                        }
                    });

                } else {
                    javafx.application.Platform.runLater(() ->
                            showAlert("Login Failed", "Invalid username or password!"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        showAlert("Database Error", "Something went wrong while connecting to the database."));
            }
        }).start();
    }

    // 🔹 CANCEL BUTTON
    @FXML
    void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) CancelButton.getScene().getWindow();
        stage.close();
    }

    // 🔹 GO TO REGISTER
    @FXML
    void goToRegisterButtonOnAction(ActionEvent event) {
        goToPage("register.fxml", "Register New Account");
    }

    // 🔸 PAGE LOADER
    private void goToPage(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load page: " + fxml);
        }
    }

    // 🔸 ALERT HELPER
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
