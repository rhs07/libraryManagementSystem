package com.example.gamef;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    // --- FXML Fields ---
    @FXML private Button CloseButton, RegisterButton, loginButtonREG;
    @FXML private TextField ConfirmPasswordField, FirstnameField, LastnameField, SetPasswordFIeld, UsernameField;
    @FXML private RadioButton studentRadioButton;
    @FXML private ToggleGroup roleToggleGroup;
    @FXML private ImageView registerLogo;
    @FXML private VBox studentFieldsVBox;
    @FXML private TextField mobileField;
    @FXML private TextField emailField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load logo
        File logoFile = new File("src/main/resources/com/example/gamef/image/register.png");
        if (logoFile.exists()) {
            registerLogo.setImage(new Image(logoFile.toURI().toString()));
        }

        // Show/hide student fields dynamically
        studentFieldsVBox.setVisible(true);
        studentFieldsVBox.setManaged(true);
        roleToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selected = (RadioButton) newValue;
            boolean isStudent = selected.getText().equals("Student");
            studentFieldsVBox.setVisible(isStudent);
            studentFieldsVBox.setManaged(isStudent);
        });
    }

    // ✅ REGISTER USER (Direct MySQL Insert)
    @FXML
    void registerUser(ActionEvent event) {
        String firstname = FirstnameField.getText().trim();
        String lastname = LastnameField.getText().trim();
        String username = UsernameField.getText().trim();
        String password = SetPasswordFIeld.getText().trim();
        String confirm = ConfirmPasswordField.getText().trim();
        String mobile = (mobileField != null) ? mobileField.getText().trim() : "";
        String email = (emailField != null) ? emailField.getText().trim() : "";

        RadioButton selectedRoleButton = (RadioButton) roleToggleGroup.getSelectedToggle();
        String role = (selectedRoleButton != null) ? selectedRoleButton.getText() : "";

        // --- Validation ---
        if (firstname.isEmpty() || lastname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields.");
            return;
        }
        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Passwords do not match.");
            return;
        }
        if (role.equalsIgnoreCase("Student") && (mobile.isEmpty() || email.isEmpty())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Mobile and Email are required for students.");
            return;
        }

        // 🔹 Run in Background Thread
        new Thread(() -> {
            try (Connection connectDB = DatabaseConnection.getConnection()) {

                // ✅ Check username duplicate
                String checkQuery = "SELECT * FROM user_account WHERE username = ?";
                PreparedStatement checkStmt = connectDB.prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    javafx.application.Platform.runLater(() ->
                            showAlert(Alert.AlertType.WARNING, "Error", "Username already exists!"));
                    return;
                }

                // ✅ Insert user into user_account
                String insertUserQuery = "INSERT INTO user_account (firstname, lastname, username, password, role) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psUser = connectDB.prepareStatement(insertUserQuery);
                psUser.setString(1, firstname);
                psUser.setString(2, lastname);
                psUser.setString(3, username);
                psUser.setString(4, password);
                psUser.setString(5, role);
                psUser.executeUpdate();

                // ✅ If Student, also add to members table
                if (role.equalsIgnoreCase("Student")) {
                    String nextMemberId;
                    String idQuery = "SELECT MAX(CAST(id AS UNSIGNED)) FROM members";
                    PreparedStatement idStmt = connectDB.prepareStatement(idQuery);
                    ResultSet idRs = idStmt.executeQuery();
                    if (idRs.next()) {
                        int maxId = idRs.getInt(1);
                        nextMemberId = String.valueOf(maxId + 1);
                    } else {
                        nextMemberId = "1";
                    }

                    String insertMember = "INSERT INTO members (id, name, username, mobile, email) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement psMember = connectDB.prepareStatement(insertMember);
                    psMember.setString(1, nextMemberId);
                    psMember.setString(2, firstname + " " + lastname);
                    psMember.setString(3, username);
                    psMember.setString(4, mobile);
                    psMember.setString(5, email);
                    psMember.executeUpdate();
                }

                // ✅ Success message
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully!");
                    goToLoginPage(event);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Something went wrong while saving user data."));
            }
        }).start();
    }

    // ❌ Close window
    @FXML
    void CloseButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) CloseButton.getScene().getWindow();
        stage.close();
    }

    // 🔁 Go to Login Page
    @FXML
    void goToLoginPage(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load Login page.");
        }
    }

    // 🔸 Helper: Alert Popup
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
