package com.example.gamef;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.scene.text.Text; // Text কম্পোনেন্ট ইম্পোর্ট করা হলো

public class addBookController {

    @FXML
    private Button BackButton;

    @FXML
    private TextField CodeTextField;

    @FXML
    private TextField authorTextField;

    @FXML
    private Button clearButton;

    @FXML
    private TextField idTextField;

    @FXML
    private TextField publisherTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Text saveSuccesfullText;

    @FXML
    private TextField titleTextField;



    @FXML
    void clearOnAction(ActionEvent event) {

        titleTextField.setText("");
        idTextField.setText("");
        authorTextField.setText("");
        publisherTextField.setText("");
        CodeTextField.setText("");
        saveSuccesfullText.setText("");
    }

    @FXML
    void goHOMEpage(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Parent root = loader.load();


            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("Library Management System - Home"); // টাইটেল আপডেট করা হলো
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading Home Page.");
        }
    }

    @FXML
    void saveOnAction(ActionEvent event) {

        String bookTitle = titleTextField.getText();
        String bookId = idTextField.getText();
        String bookAuthor = authorTextField.getText();
        String bookPublisher = publisherTextField.getText();
        String bookCode = CodeTextField.getText();


        if (bookTitle.isEmpty() || bookId.isEmpty() || bookAuthor.isEmpty() || bookPublisher.isEmpty()
                || bookCode.isEmpty()) {
            saveSuccesfullText.setText("Please fill in all fields.");
            return;
        }


        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();


        String insertFields = "INSERT INTO book_info (book_id, title, author, publisher, internal_code) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(insertFields);
            preparedStatement.setString(1, bookId);
            preparedStatement.setString(2, bookTitle);
            preparedStatement.setString(3, bookAuthor);
            preparedStatement.setString(4, bookPublisher);
            preparedStatement.setString(5, bookCode);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                saveSuccesfullText.setText("Book saved successfully!");

                clearOnAction(null);
            } else {
                saveSuccesfullText.setText("Failed to save book.");
            }

            preparedStatement.close();
            connectDB.close();

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {
                saveSuccesfullText.setText("Error: Book ID or Code already exists!");
            } else {
                saveSuccesfullText.setText("Database Error: Could not save book.");
                e.printStackTrace();
            }
        }
    }
}