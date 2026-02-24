package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Button startBtn;

    @FXML
    private Button resultBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label studentName;

    @FXML

    public void setStudentName(String name) {
        welcomeLabel.setText("WELCOME, " + name + "!");
        studentName.setText(name);
    }

    @FXML
    private void handleStart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/Exam.fxml")
            );

            Scene scene = new Scene(loader.load(),1000,600);

            Stage stage = (Stage) startBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResult() {
        System.out.println("Result clicked");
    }

    @FXML
    private void handleLogout() {
        try {
            // Close current dashboard window
            logoutBtn.getScene().getWindow().hide();

            // Open login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("EXAMORA - Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

