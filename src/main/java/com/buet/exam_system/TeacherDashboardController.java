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
import java.util.ResourceBundle;

public class TeacherDashboardController implements Initializable {

    @FXML private Button addQuestionBtn;
    @FXML private Button availableExamBtn;
    @FXML private Button leaderboardBtn;
    @FXML private Button logoutBtn;
    @FXML private Button profileBtn;

    @FXML private Label welcomeLabel;
    @FXML private Label teacherName;

    private String currentUsername;
    private String currentEmail;
    private String currentFatherEmail;
    private String currentMotherEmail;
    private final int currentRole = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (logoutBtn != null) logoutBtn.setOnAction(e -> handleLogout());
        if (profileBtn != null) profileBtn.setOnAction(e -> handleProfile());
    }

    /** Partner এর version — email, fatherEmail, motherEmail সহ */
    public void setTeacherInfo(String username, String email, String fatherEmail, String motherEmail) {
        this.currentUsername    = username;
        this.currentEmail       = email;
        this.currentFatherEmail = fatherEmail;
        this.currentMotherEmail = motherEmail;
        if (welcomeLabel != null) welcomeLabel.setText("WELCOME, " + username + "!");
        if (teacherName != null) teacherName.setText(username);
    }

    /** তোমার version — backward compatibility এর জন্য রাখা */
    public void setTeacherName(String name) {
        setTeacherInfo(name, "", "", "");
    }

    @FXML
    private void handleProfile() {
        try {
            Stage currentStage = (Stage) profileBtn.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Profile.fxml"));
            Parent root = loader.load();

            ProfileController pc = loader.getController();
            pc.setProfileInfo(currentUsername, currentEmail, currentFatherEmail, currentMotherEmail, currentRole);
            pc.setReturnTarget("TEACHER");

            currentStage.setScene(new Scene(root));
            currentStage.setTitle("My Profile");
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/CreateExam.fxml"));
            Scene scene = new Scene(loader.load());

            CreateExamController controller = loader.getController();
            controller.setTeacherName(currentUsername);

            Stage stage = (Stage) addQuestionBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAvailableExams() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/SubjectPage.fxml"));
            Parent root = loader.load();

            SubjectPageController controller = loader.getController();
            controller.setStudentMode(false, "");

            Stage stage = (Stage) availableExamBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLeaderboard() {
        System.out.println("Leaderboard clicked");
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
