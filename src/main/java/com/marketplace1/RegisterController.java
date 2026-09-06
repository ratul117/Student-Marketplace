package com.marketplace1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField studentIdField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField confirmEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField roleField;

    @FXML
    protected void handleRegister() {
        String name = nameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = "Student";

        String validation = DatabaseHandler.validateRegistration(name, studentId, email, password);
        if (!validation.isEmpty()) {
            showAlert("Check your details", validation, AlertType.WARNING);
            return;
        }
        if (confirmEmailField != null && !email.equalsIgnoreCase(confirmEmailField.getText().trim())) {
            showAlert("Email addresses do not match", "Check that both email fields contain the same address.", AlertType.WARNING);
            return;
        }
        if (confirmPasswordField == null || !password.equals(confirmPasswordField.getText())) {
            showAlert("Passwords do not match", "Enter the same password in both password fields.", AlertType.WARNING);
            return;
        }

        // ডেটাবেসের হ্যান্ডলার কল করে ইউজার সেভ করা হচ্ছে
        boolean success = DatabaseHandler.registerUser(name, studentId, email, password, role);

        if (success) {
            showAlert("Success", "Registration Successful! You can now log in.", AlertType.INFORMATION);
        } else {
            showAlert("Account already exists", "That email address or student ID is already registered. Try signing in or use different details.", AlertType.WARNING);
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
