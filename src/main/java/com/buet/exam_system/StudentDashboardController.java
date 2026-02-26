package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class StudentDashboardController {

    private String currentUsername;
    private String currentEmail;
    private String currentFatherEmail;
    private String currentMotherEmail;
    private int currentRole;

    @FXML private TableView examTable;
    @FXML private TableColumn examName;
    @FXML private TableColumn examSubject;
    @FXML private TableColumn examDate;
    @FXML private TableColumn examDuration;

    @FXML private TableView resultTable;
    @FXML private TableColumn resultExam;
    @FXML private TableColumn resultScore;
    @FXML private TableColumn resultTotal;
    @FXML private TableColumn resultStatus;

    @FXML private Button startBtn;
    @FXML private Button resultBtn;
    @FXML private Button logoutBtn;
    @FXML private Button profileBtn;

    @FXML private Label welcomeLabel;
    @FXML private Label studentName;

    @FXML
    public void initialize() {
        profileBtn.setOnAction(e -> handleProfile());
    }

    @FXML
    private void handleProfile() {
        try {
            Stage currentStage = (Stage) profileBtn.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Profile.fxml"));
            Parent root = loader.load();

            ProfileController profileController = loader.getController();
            profileController.setProfileInfo(
                    currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail,
                    currentRole);

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("My Profile");
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStudentInfo(String username, String email, String fatherEmail, String motherEmail, int role) {
        this.currentUsername    = username;
        this.currentEmail       = email;
        this.currentFatherEmail = fatherEmail;
        this.currentMotherEmail = motherEmail;
        this.currentRole        = role;

        welcomeLabel.setText("WELCOME, " + username + "!");
        studentName.setText(username);
    }

    @FXML
    private void handleStart() {
        try {
            Stage currentStage = (Stage) startBtn.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/Exam.fxml"));

            currentStage.setScene(new Scene(loader.load(), 1000, 600));
            currentStage.show();

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
            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/hello-view.fxml"));
            Parent root = loader.load();

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("EXAMORA - Login");
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}