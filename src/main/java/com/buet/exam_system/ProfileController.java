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

public class ProfileController implements Initializable {

    @FXML private Button backBtn;

    @FXML private Label profileName;
    @FXML private Label profileUsername;
    @FXML private Label profileEmail;
    @FXML private Label profileFatherEmail;
    @FXML private Label profileMotherEmail;
    @FXML private Label profileRole;
    @FXML private Label profileRoleText;

    private String username;
    private String email;
    private String fatherEmail;
    private String motherEmail;
    private int role;
    private String returnTarget = "STUDENT"; // "STUDENT" or "TEACHER"

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        backBtn.setOnAction(e -> handleBack());
    }

    public void setReturnTarget(String target) {
        this.returnTarget = target;
    }

    public void setProfileInfo(String username, String email, String fatherEmail, String motherEmail, int role) {
        this.username    = username;
        this.email       = email;
        this.fatherEmail = fatherEmail;
        this.motherEmail = motherEmail;
        this.role        = role;

        profileName.setText(username);
        profileUsername.setText(username);
        profileEmail.setText(email != null ? email : "N/A");
        profileFatherEmail.setText(fatherEmail != null ? fatherEmail : "N/A");
        profileMotherEmail.setText(motherEmail != null ? motherEmail : "N/A");

        String roleText = (role == 1) ? "Teacher" : "Student";
        profileRole.setText(roleText);
        profileRoleText.setText(roleText);
    }

    @FXML
    private void handleBack() {
        try {
            Stage currentStage = (Stage) backBtn.getScene().getWindow();

            if ("TEACHER".equals(returnTarget)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/buet/exam_system/TeacherDashboard.fxml"));
                Parent root = loader.load();

                TeacherDashboardController tc = loader.getController();
                tc.setTeacherInfo(username, email, fatherEmail, motherEmail);

                currentStage.setScene(new Scene(root));
                currentStage.setTitle("Teacher Dashboard");
                currentStage.show();

            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/buet/exam_system/StudentDashboard.fxml"));
                Parent root = loader.load();

                StudentDashboardController dc = loader.getController();
                dc.setStudentInfo(username, email, fatherEmail, motherEmail, role);

                currentStage.setScene(new Scene(root));
                currentStage.setTitle("Student Dashboard");
                currentStage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
