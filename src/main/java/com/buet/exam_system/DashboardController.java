package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Button startBtn;

    @FXML
    private Button resultBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private void handleStart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/Exam.fxml")
            );

            Scene scene = new Scene(loader.load(),600,400);

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
        System.out.println("Logout clicked");
    }
}

