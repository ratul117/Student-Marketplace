package com.marketplace1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    public void setUsername(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    @FXML
    protected void handleViewProducts() {
        // ভবিষ্যতে এখানে প্রোডাক্ট লিস্ট দেখানোর কোড থাকবে
        System.out.println("View Products Clicked");
    }

    @FXML
    protected void handleAddProduct() {
        // ভবিষ্যতে এখানে প্রোডাক্ট অ্যাড করার উইন্ডো আসবে
        System.out.println("Add Product Clicked");
    }

    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Student Login");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
