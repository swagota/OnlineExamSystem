package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class TeacherDashboardController implements Initializable {

    @FXML private Button addQuestionBtn;
    @FXML private Button availableExamBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button logoutBtn;
    @FXML private Button profileBtn;
    @FXML private Label  welcomeLabel;
    @FXML private Label  teacherName;
    @FXML private Label  totalExamsLabel;

    private String currentUsername    = "";
    private String currentEmail       = "";
    private String currentFatherEmail = "";
    private String currentMotherEmail = "";
    private final int currentRole     = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (logoutBtn  != null) logoutBtn.setOnAction(e -> handleLogout());
        if (profileBtn != null) profileBtn.setOnAction(e -> handleProfile());
    }

    public void setTeacherInfo(String username, String email,
                               String fatherEmail, String motherEmail) {
        this.currentUsername = username != null ? username : "";

        // If emails provided use them, otherwise load from DB
        if (email != null && !email.isEmpty()) {
            this.currentEmail       = email;
            this.currentFatherEmail = fatherEmail != null ? fatherEmail : "";
            this.currentMotherEmail = motherEmail != null ? motherEmail : "";
        } else {
            loadEmailsFromDb(this.currentUsername);
        }

        if (welcomeLabel != null) welcomeLabel.setText("WELCOME, " + currentUsername + "!");
        if (teacherName  != null) teacherName.setText(currentUsername);

        loadTotalExams();
    }

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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTotalExams() {
        if (totalExamsLabel == null) return;
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement("SELECT COUNT(*) FROM exams");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalExamsLabel.setText(String.valueOf(rs.getInt(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setTeacherName(String name) {
        setTeacherInfo(name, "", "", "");
    }

    @FXML
    private void handleProfile() {
        try {
            loadEmailsFromDb(currentUsername); // always fresh
            Stage stage = (Stage) profileBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Profile.fxml"));
            Parent root = loader.load();
            ProfileController pc = loader.getController();
            pc.setProfileInfo(currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail, currentRole);
            pc.setReturnTarget("TEACHER");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddQuestion() {
        try {
            loadEmailsFromDb(currentUsername);
            Stage stage = (Stage) addQuestionBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/CreateExam.fxml"));
            Parent root = loader.load();
            CreateExamController cc = loader.getController();
            cc.setTeacherInfo(currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAvailableExams() {
        try {
            loadEmailsFromDb(currentUsername);
            Stage stage = (Stage) availableExamBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/SubjectPage.fxml"));
            Parent root = loader.load();
            SubjectPageController spc = loader.getController();
            spc.setUserInfo(false, currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail, currentRole);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLeaderboard() {
        try {
            loadEmailsFromDb(currentUsername);
            Stage stage = (Stage) leaderboardBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Leaderboard.fxml"));
            Parent root = loader.load();
            LeaderboardController lc = loader.getController();
            lc.setCallerInfo(true, currentUsername, currentEmail,
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