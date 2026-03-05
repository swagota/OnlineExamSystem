package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class LeaderboardController implements Initializable {

    @FXML private Button backBtn;
    @FXML private ComboBox<String> examComboBox;
    @FXML private VBox leaderboardContainer;

    private Map<String, Integer> examMap = new HashMap<>();

    private boolean isTeacher         = false;
    private String  currentUsername    = "";
    private String  currentEmail       = "";
    private String  currentFatherEmail = "";
    private String  currentMotherEmail = "";
    private int     currentRole        = 2;

    public void setCallerInfo(boolean isTeacher, String username, String email,
                              String fatherEmail, String motherEmail, int role) {
        this.isTeacher          = isTeacher;
        this.currentUsername    = username    != null ? username    : "";
        this.currentEmail       = email       != null ? email       : "";
        this.currentFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.currentMotherEmail = motherEmail != null ? motherEmail : "";
        this.currentRole        = role;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadExams();
        showPlaceholder();
    }

    private void loadExams() {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT id, exam_name FROM exams ORDER BY id");
            ResultSet rs = ps.executeQuery();
            examComboBox.getItems().clear();
            examMap.clear();
            while (rs.next()) {
                String display = rs.getString("exam_name");
                examComboBox.getItems().add(display);
                examMap.put(display, rs.getInt("id"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleExamSelected() {
        String selected = examComboBox.getValue();
        if (selected == null) return;
        Integer examId = examMap.get(selected);
        if (examId == null) return;
        loadLeaderboard(examId);
    }

    private void loadLeaderboard(int examId) {
        leaderboardContainer.getChildren().clear();
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            PreparedStatement ps = connect.prepareStatement(
                    "SELECT username, MAX(score) as best_score, total " +
                            "FROM results WHERE exam_id = ? " +
                            "GROUP BY username, total " +
                            "ORDER BY best_score DESC");
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();

            int rank = 1;
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                String username  = rs.getString("username");
                int    bestScore = rs.getInt("best_score");
                int    total     = rs.getInt("total");
                double pct       = total > 0 ? (bestScore * 100.0 / total) : 0;
                leaderboardContainer.getChildren().add(
                        createRow(rank, username, bestScore, total, pct));
                rank++;
            }

            if (!hasResults) showNoResults();

        } catch (Exception e) {
            e.printStackTrace();
            showNoResults();
        }
    }

    private AnchorPane createRow(int rank, String username,
                                 int score, int total, double pct) {
        AnchorPane row = new AnchorPane();
        row.setPrefWidth(798);
        row.setPrefHeight(56);
        row.setMinHeight(56);

        boolean isEven = rank % 2 == 0;
        row.setStyle("-fx-background-color: " + (isEven ? "#f8faff" : "white") + ";");

        // Rank number badge — gold/silver/bronze/blue
        String rankBg;
        if      (rank == 1) rankBg = "#f39c12";
        else if (rank == 2) rankBg = "#7f8c8d";
        else if (rank == 3) rankBg = "#cd6133";
        else                rankBg = "#6a9ae7";

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.setLayoutX(20);
        rankLabel.setLayoutY(13);
        rankLabel.setPrefWidth(32);
        rankLabel.setPrefHeight(30);
        rankLabel.setStyle(
                "-fx-background-color: " + rankBg + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-alignment: center;");

        // Username
        Label nameLabel = new Label(username);
        nameLabel.setLayoutX(100);
        nameLabel.setLayoutY(16);
        nameLabel.setPrefWidth(260);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a2a4a; -fx-font-weight: bold;");

        // Score
        Label scoreLabel = new Label(score + " / " + total);
        scoreLabel.setLayoutX(400);
        scoreLabel.setLayoutY(16);
        scoreLabel.setPrefWidth(150);
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6a9ae7; -fx-font-weight: bold;");

        // Percentage badge
        String pctColor = pct >= 80 ? "#27ae60" : pct >= 50 ? "#e67e22" : "#e74c3c";
        Label pctLabel = new Label(String.format("%.1f%%", pct));
        pctLabel.setLayoutX(565);
        pctLabel.setLayoutY(11);
        pctLabel.setPrefWidth(115);
        pctLabel.setPrefHeight(34);
        pctLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;" +
                        "-fx-background-color: " + pctColor + ";" +
                        "-fx-background-radius: 20px; -fx-alignment: center;");

        // Divider
        Label divider = new Label();
        divider.setLayoutX(0);
        divider.setLayoutY(55);
        divider.setPrefWidth(798);
        divider.setStyle("-fx-border-color: #eef2ff; -fx-border-width: 1px 0 0 0;");

        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel, pctLabel, divider);
        return row;
    }

    private void showPlaceholder() {
        leaderboardContainer.getChildren().clear();
        Label msg = new Label("👆  Select an exam above to see the leaderboard");
        msg.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px; " +
                "-fx-font-style: italic; -fx-padding: 40px 0 0 200px;");
        leaderboardContainer.getChildren().add(msg);
    }

    private void showNoResults() {
        leaderboardContainer.getChildren().clear();
        Label msg = new Label("No results yet for this exam.");
        msg.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px; " +
                "-fx-font-style: italic; -fx-padding: 40px 0 0 250px;");
        leaderboardContainer.getChildren().add(msg);
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            FXMLLoader loader;
            Parent root;

            if (isTeacher) {
                loader = new FXMLLoader(getClass().getResource(
                        "/com/buet/exam_system/teacherDashboard.fxml"));
                root = loader.load();
                TeacherDashboardController tc = loader.getController();
                tc.setTeacherInfo(currentUsername, currentEmail,
                        currentFatherEmail, currentMotherEmail);
            } else {
                loader = new FXMLLoader(getClass().getResource(
                        "/com/buet/exam_system/StudentDashboard.fxml"));
                root = loader.load();
                StudentDashboardController dc = loader.getController();
                dc.setStudentInfo(currentUsername, currentEmail,
                        currentFatherEmail, currentMotherEmail, currentRole);
            }

            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}