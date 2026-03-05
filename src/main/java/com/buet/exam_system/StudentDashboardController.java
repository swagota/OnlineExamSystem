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

import java.sql.*;

public class StudentDashboardController {

    private String currentUsername    = "";
    private String currentEmail       = "";
    private String currentFatherEmail = "";
    private String currentMotherEmail = "";
    private int    currentRole        = 2;

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
        if (profileBtn != null) profileBtn.setOnAction(e -> handleProfile());
    }

    public void setStudentInfo(String username, String email, String fatherEmail,
                               String motherEmail, int role) {
        this.currentUsername = username != null ? username : "";
        this.currentRole     = role;

        // If emails are provided use them, otherwise load from DB
        if (email != null && !email.isEmpty()) {
            this.currentEmail       = email;
            this.currentFatherEmail = fatherEmail != null ? fatherEmail : "";
            this.currentMotherEmail = motherEmail != null ? motherEmail : "";
        } else {
            loadEmailsFromDb(this.currentUsername);
        }

        if (welcomeLabel != null) welcomeLabel.setText("WELCOME, " + currentUsername + "!");
        if (studentName  != null) studentName.setText(currentUsername);
    }

    /** Always fetch fresh from DB using username */
    private void loadEmailsFromDb(String username) {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT email, father_email, mother_email FROM data WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.currentEmail       = rs.getString("email")        != null ? rs.getString("email")        : "";
                this.currentFatherEmail = rs.getString("father_email") != null ? rs.getString("father_email") : "";
                this.currentMotherEmail = rs.getString("mother_email") != null ? rs.getString("mother_email") : "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        try {
            // Always reload from DB before opening profile
            loadEmailsFromDb(currentUsername);

            Stage stage = (Stage) profileBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Profile.fxml"));
            Parent root = loader.load();
            ProfileController pc = loader.getController();
            pc.setReturnTarget("STUDENT");
            pc.setProfileInfo(currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail, currentRole);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleStart() {
        try {
            // Always reload from DB before navigating
            loadEmailsFromDb(currentUsername);

            Stage stage = (Stage) startBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/SubjectPage.fxml"));
            Parent root = loader.load();
            SubjectPageController spc = loader.getController();
            spc.setUserInfo(true, currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail, currentRole);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleResult() {
        try {
            loadEmailsFromDb(currentUsername);
            Stage stage = (Stage) resultBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Leaderboard.fxml"));
            Parent root = loader.load();
            LeaderboardController lc = loader.getController();
            lc.setCallerInfo(false, currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail, currentRole);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/hello-view.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("EXAMORA - Login");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}