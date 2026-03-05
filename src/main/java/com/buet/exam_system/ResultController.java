package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ResultController {

    @FXML private Label emojiLabel;
    @FXML private Label scoreLabel;
    @FXML private Label percentLabel;
    @FXML private Label correctLabel;
    @FXML private Label wrongLabel;
    @FXML private Label totalLabel;
    @FXML private Button backBtn;

    private String studentUsername    = "";
    private String studentEmail       = "";
    private String studentFatherEmail = "";
    private String studentMotherEmail = "";
    private int    studentRole        = 2;

    public void setStudentUsername(String username) {
        this.studentUsername = username != null ? username : "";
    }

    public void setStudentInfo(String username, String email,
                               String fatherEmail, String motherEmail, int role) {
        this.studentUsername    = username    != null ? username    : "";
        this.studentEmail       = email       != null ? email       : "";
        this.studentFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.studentMotherEmail = motherEmail != null ? motherEmail : "";
        this.studentRole        = role;
    }

    public void setResult(int score, int total) {
        double pct   = total > 0 ? (score * 100.0 / total) : 0;
        int    wrong = total - score;
        String emoji = pct >= 60 ? "🎉" : "📚";

        emojiLabel.setText(emoji);
        scoreLabel.setText(score + " / " + total);
        percentLabel.setText(String.format("%.1f%%  correct", pct));
        correctLabel.setText(String.valueOf(score));
        wrongLabel.setText(String.valueOf(wrong));
        totalLabel.setText(String.valueOf(total));
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/SubjectPage.fxml"));
            Parent root = loader.load();
            SubjectPageController spc = loader.getController();
            spc.setUserInfo(true, studentUsername, studentEmail,
                    studentFatherEmail, studentMotherEmail, studentRole);
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}