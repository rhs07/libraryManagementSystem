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

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


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
import javafx.scene.text.Text;

public class addMemberController {

    @FXML
    private TextField name;

    @FXML
    private TextField id;

    @FXML
    private TextField mobile;

    @FXML
    private TextField email;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton; // fx:id is still cancelButton

    @FXML
    private Button BackButton;
    private Image event;

    @FXML
    public void addMember(ActionEvent actionEvent) {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String memberName = name.getText();
        String memberID = id.getText();
        String memberMobile = mobile.getText();
        String memberEmail = email.getText();

        if (memberName.isEmpty() || memberID.isEmpty() || memberMobile.isEmpty() || memberEmail.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please enter all fields");
            return;
        }

        String insertQuery = "INSERT INTO members (id, name, mobile, email) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement statement = connectDB.prepareStatement(insertQuery);
            statement.setString(1, memberID);
            statement.setString(2, memberName);
            statement.setString(3, memberMobile);
            statement.setString(4, memberEmail);

            statement.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Member successfully added!");
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error!", "Failed to add member.");
        }
    }

    // "Clear" button-e click korle ei function-ti kaaj korbe
    // Function-er naam poriborton kora hoyeche
    @FXML
    public void clearButtonAction(ActionEvent actionEvent) {
        clearFields();
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
    void clearFields(ActionEvent event) {
        name.clear();
        id.clear();
        mobile.clear();
        email.clear();

    }

    @FXML
    private void clearFields() {

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}