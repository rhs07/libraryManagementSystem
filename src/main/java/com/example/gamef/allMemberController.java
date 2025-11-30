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

public class allMemberController implements Initializable {

    @FXML
    private TableView<Member> tableView;
    @FXML
    private TableColumn<Member, String> idCol;
    @FXML
    private TableColumn<Member, String> nameCol;
    @FXML
    private TableColumn<Member, String> mobileCol;
    @FXML
    private TableColumn<Member, String> emailCol;
    @FXML
    private Button BackButton;
    @FXML
    private Button deleteButton;

    ObservableList<Member> memberList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMembers();
    }

    private void loadMembers() {
        memberList.clear();
        String query = "SELECT id, name, mobile, email FROM members";

        try (Connection connectDB = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                memberList.add(new Member(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("mobile"),
                        resultSet.getString("email")
                ));
            }

            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            mobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

            tableView.setItems(memberList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ UPDATED: Deletes the selected member and their corresponding user account in a single transaction.
     */
    @FXML
    private void deleteSelectedMember(ActionEvent event) {
        Member selectedMember = tableView.getSelectionModel().getSelectedItem();

        if (selectedMember == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a member to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Member and User Account?");
        confirm.setContentText("Are you sure you want to delete member ID: " + selectedMember.getId() + "?\nThis will also delete their login account.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            Connection connectDB = null;
            String memberIdToDelete = selectedMember.getId();
            String usernameToDelete = null;

            try {
                connectDB = DatabaseConnection.getConnection();
                // --- STEP 1: Find the associated username from the members table ---
                String getUsernameQuery = "SELECT username FROM members WHERE id = ?";
                try (PreparedStatement ps = connectDB.prepareStatement(getUsernameQuery)) {
                    ps.setString(1, memberIdToDelete);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        usernameToDelete = rs.getString("username");
                    }
                }

                // --- STEP 2: Start Transaction ---
                connectDB.setAutoCommit(false);

                // --- STEP 3: Delete from user_account table (if a username is linked) ---
                if (usernameToDelete != null && !usernameToDelete.isEmpty()) {
                    String deleteUserQuery = "DELETE FROM user_account WHERE username = ?";
                    try (PreparedStatement ps = connectDB.prepareStatement(deleteUserQuery)) {
                        ps.setString(1, usernameToDelete);
                        ps.executeUpdate();
                    }
                }

                // --- STEP 4: Delete from members table ---
                String deleteMemberQuery = "DELETE FROM members WHERE id = ?";
                int rowsDeleted;
                try (PreparedStatement ps = connectDB.prepareStatement(deleteMemberQuery)) {
                    ps.setString(1, memberIdToDelete);
                    rowsDeleted = ps.executeUpdate();
                }

                // --- STEP 5: Commit the transaction if everything is successful ---
                connectDB.commit();

                if (rowsDeleted > 0) {
                    memberList.remove(selectedMember); // Remove from table view instantly
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Member and associated user account deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Member not found or already deleted!");
                }

            } catch (SQLException e) {
                // --- STEP 6: Rollback the transaction if any error occurs ---
                try {
                    if (connectDB != null) connectDB.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace(); // Log rollback failure
                }
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete member. The operation was rolled back.");
                e.printStackTrace();
            } finally {
                // --- STEP 7: Close the connection ---
                try {
                    if (connectDB != null) {
                        connectDB.setAutoCommit(true);
                        connectDB.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}