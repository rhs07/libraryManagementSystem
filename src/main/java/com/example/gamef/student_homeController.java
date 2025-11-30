package com.example.gamef;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class student_homeController implements Initializable {


    @FXML private Button minimizeButton, closeButton;
    @FXML private Button RequestissueBookButton, logoutButton, AllBookButton, RequestisuuedBookButton, searchBookButton, homeButton;
    @FXML private TextField issue_bookID;
    @FXML private Text bookAuthorText, bookNameText, bookStatusText, memberIdText, memberNameText;
    @FXML private AnchorPane rootPane;

    private String loggedInMemberId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserSession session = UserSession.getInstance();

        if (session != null) {
            loggedInMemberId = session.getMemberId();
            String memberName = session.getFullName();

            if (loggedInMemberId != null && !loggedInMemberId.equals("N/A")) {
                memberNameText.setText("Member Name: " + memberName);
                memberIdText.setText("Member ID: " + loggedInMemberId);
            } else {
                memberNameText.setText("Member Name: Not Logged In");
                memberIdText.setText("Member ID: N/A");
                RequestissueBookButton.setDisable(true);
            }
        } else {
            memberNameText.setText("Member Name: Not Logged In");
            memberIdText.setText("Member ID: N/A");
            RequestissueBookButton.setDisable(true);
        }
    }

    // --- Issue Book Request ---
    @FXML
    void issueBook(ActionEvent event) {
        String bookID = issue_bookID.getText().trim();
        if (bookID.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a Book ID.");
            return;
        }

        if (loggedInMemberId == null || loggedInMemberId.equals("N/A")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please log in again.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkBookQuery = "SELECT is_available FROM book_info WHERE book_id = ?";
            PreparedStatement psBook = conn.prepareStatement(checkBookQuery);
            psBook.setString(1, bookID);
            ResultSet rsBook = psBook.executeQuery();

            if (!rsBook.next()) {
                showAlert(Alert.AlertType.ERROR, "Not Found", "Book ID not found!");
                return;
            }

            boolean isAvailable = rsBook.getBoolean("is_available");
            if (!isAvailable) {
                showAlert(Alert.AlertType.ERROR, "Unavailable", "This book is not available for issue.");
                return;
            }

            String requestQuery = "INSERT INTO issue_requests (book_id, member_id, status) VALUES (?, ?, 'pending')";
            PreparedStatement psRequest = conn.prepareStatement(requestQuery);
            psRequest.setString(1, bookID);
            psRequest.setString(2, loggedInMemberId);

            if (psRequest.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book issue request sent to librarian.");
                issue_bookID.clear();
                clearBookInfo();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Could not send issue request.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database operation failed!");
        }
    }

    // --- Logout ---
    @FXML
    void logoutOnAction(ActionEvent event) {
        UserSession.clearSession();
        loadPage("Login.fxml", "Login Page");
    }

    // --- Page Navigation ---
    @FXML void goHomePage(ActionEvent event) { loadPage("student_home.fxml", "Student Dashboard"); }
    @FXML void searchbook(ActionEvent event) { loadPage("student_searchBook.fxml", "Search Book"); }
    @FXML void allbook(ActionEvent event) { loadPage("student_allBook.fxml", "All Books"); }
    @FXML void issuedbook(ActionEvent event) { showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Issued books list coming soon."); }

    // --- Load Book Info ---
    @FXML
    void loadBookInfo(ActionEvent event) {
        String bookID = issue_bookID.getText().trim();
        if (bookID.isEmpty()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT title, author, is_available FROM book_info WHERE book_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, bookID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                bookNameText.setText("Book Name: " + rs.getString("title"));
                bookAuthorText.setText("Author: " + rs.getString("author"));
                bookStatusText.setText("Status: " + (rs.getBoolean("is_available") ? "Available" : "Not Available"));
            } else {
                clearBookInfo();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Helper Methods ---
    private void loadPage(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearBookInfo() {
        bookNameText.setText("Book Name:");
        bookAuthorText.setText("Author:");
        bookStatusText.setText("Status:");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleCloseButtonAction(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMinimizeButtonAction(ActionEvent event) {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }



}
