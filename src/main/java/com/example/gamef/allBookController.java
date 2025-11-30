package com.example.gamef;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class allBookController implements Initializable {

    @FXML
    private TableView<Book> tableView;
    @FXML
    private TableColumn<Book, String> idCol;
    @FXML
    private TableColumn<Book, String> titleCol;
    @FXML
    private TableColumn<Book, String> authorCol;
    @FXML
    private TableColumn<Book, String> publisherCol;
    @FXML
    private TableColumn<Book, String> codeCol;
    @FXML
    private Button BackButton;
    @FXML
    private Button deleteButton;

    ObservableList<Book> bookList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadBooks();
    }

    private void loadBooks() {
        bookList.clear();
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String query = "SELECT book_id, title, author, publisher, " +
                "CASE WHEN is_available = 1 THEN 'Yes' ELSE 'No' END AS available " +
                "FROM book_info";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String bookId = resultSet.getString("book_id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String publisher = resultSet.getString("publisher");
                String available = resultSet.getString("available");
                bookList.add(new Book(bookId, title, author, publisher, available));
            }

            idCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
            publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
            codeCol.setCellValueFactory(new PropertyValueFactory<>("available"));

            tableView.setItems(bookList);

            preparedStatement.close();
            connectDB.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** ✅ DELETE SELECTED BOOK BUTTON ACTION */
    @FXML
    private void deleteSelectedBook(ActionEvent event) {
        Book selectedBook = tableView.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a book to delete.");
            alert.showAndWait();
            return;
        }

        String bookId = selectedBook.getBookId();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this book?");
        confirm.setContentText("Book ID: " + bookId);
        if (confirm.showAndWait().get() == ButtonType.OK) {

            DatabaseConnection connectNow = new DatabaseConnection();
            Connection connectDB = connectNow.getConnection();

            String deleteQuery = "DELETE FROM book_info WHERE book_id = ?";

            try (PreparedStatement preparedStatement = connectDB.prepareStatement(deleteQuery)) {
                preparedStatement.setString(1, bookId);
                int rows = preparedStatement.executeUpdate();

                if (rows > 0) {
                    bookList.remove(selectedBook); // remove from table instantly
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Deleted");
                    success.setHeaderText(null);
                    success.setContentText("Book deleted successfully!");
                    success.showAndWait();
                } else {
                    Alert fail = new Alert(Alert.AlertType.ERROR);
                    fail.setTitle("Error");
                    fail.setHeaderText(null);
                    fail.setContentText("Book not found or already deleted!");
                    fail.showAndWait();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /** 🏠 HOME BUTTON ACTION */
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
        }
    }
}
