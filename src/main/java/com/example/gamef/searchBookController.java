package com.example.gamef;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class searchBookController {

    @FXML
    private Button backButton;

    @FXML
    private TableColumn<Book, String> colAuthor;

    @FXML
    private TableColumn<Book, String> colAvailable;

    @FXML
    private TableColumn<Book, String> colBookId;

    @FXML
    private TableColumn<Book, String> colPublisher;

    @FXML
    private TableColumn<Book, String> colTitle;

    @FXML
    private Button refreshButton;

    @FXML
    private Button searchButton;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Book> tableView;

    // ✅ ObservableList for table data
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Column mapping
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colPublisher.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Load all books on startup
        loadAllBooks(null);
    }

    // ✅ Load All Books from Database
    @FXML
    void loadAllBooks(ActionEvent event) {
        bookList.clear();
        Connection conn = DatabaseConnection.getConnection();

        String query = "SELECT book_id, title, author, publisher, " +
                "CASE WHEN is_available = 1 THEN 'Yes' ELSE 'No' END AS available " +
                "FROM book_info";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                bookList.add(new Book(
                        rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("available")
                ));
            }

            tableView.setItems(bookList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Search Book by ID or Title
    @FXML
    void searchBook(ActionEvent event) {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadAllBooks(null);
            return;
        }

        bookList.clear();
        Connection conn = DatabaseConnection.getConnection();

        String query = "SELECT book_id, title, author, publisher, " +
                "CASE WHEN is_available = 1 THEN 'Yes' ELSE 'No' END AS available " +
                "FROM book_info WHERE book_id LIKE ? OR title LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                bookList.add(new Book(
                        rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("available")
                ));
            }

            tableView.setItems(bookList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Go Back to Home Page
    @FXML
    void goHOMEpage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Library Management System - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading Home Page.");
        }
    }
}
