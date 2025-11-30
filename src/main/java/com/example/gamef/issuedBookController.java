package com.example.gamef;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class issuedBookController implements Initializable {

    @FXML
    private TableView<IssuedBook> tableView;
    @FXML
    private TableColumn<IssuedBook, String> bookIdCol;
    @FXML
    private TableColumn<IssuedBook, String> bookTitleCol;
    @FXML
    private TableColumn<IssuedBook, String> memberIdCol;
    @FXML
    private TableColumn<IssuedBook, String> memberNameCol;
    @FXML
    private TableColumn<IssuedBook, Timestamp> issueTimeCol;
    @FXML
    private TableColumn<IssuedBook, Integer> renewCountCol;

    ObservableList<IssuedBook> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initCol();
        loadData();
    }

    private void initCol() {
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        bookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        memberIdCol.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        memberNameCol.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        issueTimeCol.setCellValueFactory(new PropertyValueFactory<>("issueTime"));
        renewCountCol.setCellValueFactory(new PropertyValueFactory<>("renewCount"));
    }

    private void loadData() {
        list.clear();
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Query to join issued_books, book_info, and members tables
        String query = "SELECT i.book_id, b.title, i.member_id, m.name, i.issue_time, i.renew_count " +
                "FROM issued_books i " +
                "JOIN book_info b ON i.book_id = b.book_id " +
                "JOIN members m ON i.member_id = m.id";

        try (PreparedStatement statement = connectDB.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String bookId = rs.getString("book_id");
                String bookTitle = rs.getString("title");
                String memberId = rs.getString("member_id");
                String memberName = rs.getString("name");
                Timestamp issueTime = rs.getTimestamp("issue_time");
                int renewCount = rs.getInt("renew_count");

                list.add(new IssuedBook(bookId, bookTitle, memberId, memberName, issueTime, renewCount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableView.setItems(list);
    }

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