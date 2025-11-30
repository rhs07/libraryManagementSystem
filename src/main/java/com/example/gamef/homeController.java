package com.example.gamef;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class homeController implements Initializable {

    // --- EXISTING FXML FIELDS ---
    @FXML private Button AddsBookButton, AddsMemberButton, AllBookButton, AllMemberButton, isuuedBookButton, searchBookButton, searchMemberButton, logoutButton;
    @FXML private TextField issue_bookID, issue_memberID, return_bookID;
    @FXML private Text bookNameText, bookAuthorText, bookStatusText, memberNameText, memberContactText, issueDetailsText;
    @FXML private Button issueBookButton, returnBookButton, renewBookButton;
    @FXML private AnchorPane rootPane;
    @FXML private Button closeButton, minimizeButton;

    // --- NEW FXML FIELDS FOR REQUESTS TAB ---
    @FXML private TableView<IssueRequest> requestsTableView;
    @FXML private TableColumn<IssueRequest, Integer> colRequestID;
    @FXML private TableColumn<IssueRequest, String> colBookID;
    @FXML private TableColumn<IssueRequest, String> colBookTitle;
    @FXML private TableColumn<IssueRequest, String> colMemberID;
    @FXML private TableColumn<IssueRequest, String> colMemberName;
    @FXML private Button acceptRequestButton;
    @FXML private Button rejectRequestButton;

    private ObservableList<IssueRequest> requestList = FXCollections.observableArrayList();

    private double xOffset = 0;
    private double yOffset = 0;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Make window draggable
        rootPane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        rootPane.setOnMouseDragged(event -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Initialize and load the pending requests table
        initializeRequestTable();
        loadPendingRequests();
    }

    // --- PENDING REQUESTS METHODS ---

    private void initializeRequestTable() {
        colRequestID.setCellValueFactory(new PropertyValueFactory<>("requestID"));
        colBookID.setCellValueFactory(new PropertyValueFactory<>("bookID"));
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colMemberID.setCellValueFactory(new PropertyValueFactory<>("memberID"));
        colMemberName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
    }

    private void loadPendingRequests() {
        requestList.clear();
        String query = "SELECT ir.request_id, ir.book_id, b.title, ir.member_id, m.name " +
                "FROM issue_requests ir " +
                "JOIN book_info b ON ir.book_id = b.book_id " +
                "JOIN members m ON ir.member_id = m.id " +
                "WHERE ir.status = 'pending'";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                requestList.add(new IssueRequest(
                        rs.getInt("request_id"),
                        rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getString("member_id"),
                        rs.getString("name")
                ));
            }
            requestsTableView.setItems(requestList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load pending requests.");
        }
    }

    @FXML
    void acceptRequest(ActionEvent event) {
        IssueRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a request to accept.");
            return;
        }

        String bookID = selectedRequest.getBookID();
        String memberID = selectedRequest.getMemberID();
        int requestID = selectedRequest.getRequestID();

        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // 1. Update request status to 'accepted'
            PreparedStatement psUpdateReq = conn.prepareStatement("UPDATE issue_requests SET status = 'accepted' WHERE request_id = ?");
            psUpdateReq.setInt(1, requestID);

            // 2. Insert into issued_books table
            PreparedStatement psIssue = conn.prepareStatement("INSERT INTO issued_books (book_id, member_id) VALUES (?, ?)");
            psIssue.setString(1, bookID);
            psIssue.setString(2, memberID);

            // 3. Update book status to not available
            PreparedStatement psUpdateBook = conn.prepareStatement("UPDATE book_info SET is_available = false WHERE book_id = ?");
            psUpdateBook.setString(1, bookID);

            if (psUpdateReq.executeUpdate() > 0 && psIssue.executeUpdate() > 0 && psUpdateBook.executeUpdate() > 0) {
                conn.commit(); // Commit transaction
                showAlert(Alert.AlertType.INFORMATION, "Success", "Request accepted and book issued successfully.");
            } else {
                conn.rollback(); // Rollback if something failed
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to accept the request.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "A database error occurred.");
        }
        loadPendingRequests(); // Refresh the table
    }

    @FXML
    void rejectRequest(ActionEvent event) {
        IssueRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a request to reject.");
            return;
        }

        String updateQuery = "UPDATE issue_requests SET status = 'rejected' WHERE request_id = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {

            ps.setInt(1, selectedRequest.getRequestID());
            if (ps.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Request has been rejected.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject the request.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "A database error occurred.");
        }
        loadPendingRequests(); // Refresh the table
    }

    // --- CUSTOM WINDOW ACTION HANDLERS ---
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


    // --- EXISTING METHODS ---
    @FXML
    void addbook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addBook.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AddBook Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logoutOnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void addmember(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addMember.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AddMember Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void allbook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("allBook.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AllBook Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void allmember(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("allMember.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AllMember Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void issuedbook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("issuedBook.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("IssuedBook Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void searchbook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("searchBook.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SearchBook Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void searchmember(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("searchMember.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SearchMember Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- ISSUE/RETURN METHODS ---

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void loadBookInfo(ActionEvent event) {
        String bookID = issue_bookID.getText();
        if (bookID.isEmpty()) {
            return;
        }

        String query = "SELECT title, author, is_available FROM book_info WHERE book_id = ?";
        Connection conn = new DatabaseConnection().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, bookID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                bookNameText.setText("Book Name: " + rs.getString("title"));
                bookAuthorText.setText("Author: " + rs.getString("author"));
                String status = rs.getBoolean("is_available") ? "Available" : "Not Available";
                bookStatusText.setText("Status: " + status);
            } else {
                bookNameText.setText("Book Name: Not Found");
                bookAuthorText.setText("Author: ");
                bookStatusText.setText("Status: ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void loadMemberInfo(ActionEvent event) {
        String memberID = issue_memberID.getText();
        if (memberID.isEmpty()) {
            return;
        }
        String query = "SELECT name, mobile FROM members WHERE id = ?";
        Connection conn = new DatabaseConnection().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, memberID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                memberNameText.setText("Member Name: " + rs.getString("name"));
                memberContactText.setText("Contact: " + rs.getString("mobile"));
            } else {
                memberNameText.setText("Member Name: Not Found");
                memberContactText.setText("Contact: ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void issueBook(ActionEvent event) {
        String bookID = issue_bookID.getText();
        String memberID = issue_memberID.getText();

        if (bookID.isEmpty() || memberID.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter both Book ID and Member ID.");
            return;
        }

        Connection conn = new DatabaseConnection().getConnection();
        try {
            String checkBookQuery = "SELECT is_available FROM book_info WHERE book_id = ?";
            PreparedStatement psBook = conn.prepareStatement(checkBookQuery);
            psBook.setString(1, bookID);
            ResultSet rsBook = psBook.executeQuery();
            if (!rsBook.next() || !rsBook.getBoolean("is_available")) {
                showAlert(Alert.AlertType.ERROR, "Error", "This book is not available for issue or does not exist.");
                return;
            }

            String issueQuery = "INSERT INTO issued_books (book_id, member_id) VALUES (?, ?)";
            PreparedStatement psIssue = conn.prepareStatement(issueQuery);
            psIssue.setString(1, bookID);
            psIssue.setString(2, memberID);

            String updateBookQuery = "UPDATE book_info SET is_available = false WHERE book_id = ?";
            PreparedStatement psUpdate = conn.prepareStatement(updateBookQuery);
            psUpdate.setString(1, bookID);

            if (psIssue.executeUpdate() > 0 && psUpdate.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book issued successfully.");
                issue_bookID.clear();
                issue_memberID.clear();
                bookNameText.setText("Book Name: ");
                bookAuthorText.setText("Author: ");
                bookStatusText.setText("Status: ");
                memberNameText.setText("Member Name: ");
                memberContactText.setText("Contact: ");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to issue book.");
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showAlert(Alert.AlertType.ERROR, "Error", "This book is already issued.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not issue book.");
                e.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    void loadIssueInfo(ActionEvent event) {
        String bookID = return_bookID.getText();
        if (bookID.isEmpty()) {
            issueDetailsText.setText("Please enter a Book ID.");
            return;
        }

        String query = "SELECT i.member_id, i.issue_time, i.renew_count, m.name FROM issued_books i JOIN members m ON i.member_id = m.id WHERE i.book_id = ?";
        Connection conn = new DatabaseConnection().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, bookID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp issueTime = rs.getTimestamp("issue_time");
                int renewCount = rs.getInt("renew_count");
                issueDetailsText.setText(
                        "Issued to: " + rs.getString("name") + "\n" +
                                "Member ID: " + rs.getString("member_id") + "\n" +
                                "Issue Date: " + issueTime.toString() + "\n" +
                                "Renew Count: " + renewCount
                );
            } else {
                issueDetailsText.setText("This book is not currently issued.");
            }
        } catch (SQLException e) {
            issueDetailsText.setText("Error retrieving issue information.");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    void returnBook(ActionEvent event) {
        String bookID = return_bookID.getText();
        if (bookID.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a Book ID to return.");
            return;
        }

        String deleteQuery = "DELETE FROM issued_books WHERE book_id = ?";
        String updateQuery = "UPDATE book_info SET is_available = true WHERE book_id = ?";
        Connection conn = new DatabaseConnection().getConnection();
        try (PreparedStatement psDelete = conn.prepareStatement(deleteQuery);
             PreparedStatement psUpdate = conn.prepareStatement(updateQuery)) {

            psDelete.setString(1, bookID);
            psUpdate.setString(1, bookID);

            if (psDelete.executeUpdate() > 0) {
                psUpdate.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully.");
                return_bookID.clear();
                issueDetailsText.setText("Issue details will be displayed here...");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "This book is not currently issued or does not exist.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not return book.");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    void renewBook(ActionEvent event) {
        String bookID = return_bookID.getText();
        if (bookID.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a Book ID to renew.");
            return;
        }

        String updateQuery = "UPDATE issued_books SET renew_count = renew_count + 1, issue_time = current_timestamp() WHERE book_id = ?";
        Connection conn = new DatabaseConnection().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setString(1, bookID);

            if (ps.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book renewed successfully.");
                loadIssueInfo(null); // Reload info
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "This book is not currently issued or does not exist.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not renew book.");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}