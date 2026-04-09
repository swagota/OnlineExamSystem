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

            // Get best result per student for this exam
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT r.username, MAX(r.score) as best_score, r.total, " +
                            "  (SELECT r2.id FROM results r2 WHERE r2.username = r.username " +
                            "   AND r2.exam_id = ? ORDER BY r2.score DESC LIMIT 1) as result_id " +
                            "FROM results r WHERE r.exam_id = ? " +
                            "GROUP BY r.username, r.total " +
                            "ORDER BY best_score DESC");
            ps.setInt(1, examId);
            ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery();

            int rank = 1;
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                String username  = rs.getString("username");
                double bestScore = rs.getDouble("best_score");
                int    total     = rs.getInt("total");
                int    resultId  = rs.getInt("result_id");
                double pct       = total > 0 ? (bestScore * 100.0 / total) : 0;

                // Show "View Result" for teacher (all) or student (own only)
                boolean showBtn = isTeacher || username.equals(currentUsername);

                leaderboardContainer.getChildren().add(
                        createRow(rank, username, bestScore, total, pct, resultId, showBtn));
                rank++;
            }

            if (!hasResults) showNoResults();

        } catch (Exception e) {
            e.printStackTrace();
            showNoResults();
        }
    }

    private AnchorPane createRow(int rank, String username,
                                 double score, int total, double pct,
                                 int resultId, boolean showViewBtn) {
        AnchorPane row = new AnchorPane();
        row.setPrefWidth(798);
        row.setPrefHeight(56);
        row.setMinHeight(56);

        boolean isEven = rank % 2 == 0;
        row.setStyle("-fx-background-color: " + (isEven ? "#f8faff" : "white") + ";");

        // Rank badge
        String rankBg;
        if      (rank == 1) rankBg = "#f39c12";
        else if (rank == 2) rankBg = "#7f8c8d";
        else if (rank == 3) rankBg = "#cd6133";
        else                rankBg = "#6a9ae7";

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.setLayoutX(20); rankLabel.setLayoutY(13);
        rankLabel.setPrefWidth(32); rankLabel.setPrefHeight(30);
        rankLabel.setStyle("-fx-background-color:" + rankBg + ";-fx-text-fill:white;" +
                "-fx-font-size:14px;-fx-font-weight:bold;" +
                "-fx-background-radius:50%;-fx-alignment:center;");

        // Username
        Label nameLabel = new Label(username);
        nameLabel.setLayoutX(70); nameLabel.setLayoutY(16);
        nameLabel.setPrefWidth(220);
        nameLabel.setStyle("-fx-font-size:14px;-fx-text-fill:#1a2a4a;-fx-font-weight:bold;");

        // Score
        Label scoreLabel = new Label(score + " / " + total);
        scoreLabel.setLayoutX(310); scoreLabel.setLayoutY(16);
        scoreLabel.setPrefWidth(140);
        scoreLabel.setStyle("-fx-font-size:14px;-fx-text-fill:#6a9ae7;-fx-font-weight:bold;");

        // Percentage
        String pctColor = pct >= 80 ? "#27ae60" : pct >= 50 ? "#e67e22" : "#e74c3c";
        Label pctLabel = new Label(String.format("%.1f%%", pct));
        pctLabel.setLayoutX(460); pctLabel.setLayoutY(11);
        pctLabel.setPrefWidth(100); pctLabel.setPrefHeight(34);
        pctLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:white;" +
                "-fx-background-color:" + pctColor + ";-fx-background-radius:20px;-fx-alignment:center;");

        // Divider
        Label divider = new Label();
        divider.setLayoutX(0); divider.setLayoutY(55);
        divider.setPrefWidth(798);
        divider.setStyle("-fx-border-color:#eef2ff;-fx-border-width:1px 0 0 0;");

        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel, pctLabel, divider);

        // View Result button
        if (showViewBtn) {
            Button viewBtn = new Button("📋 View Result");
            viewBtn.setLayoutX(590); viewBtn.setLayoutY(13);
            viewBtn.setPrefHeight(30); viewBtn.setPrefWidth(120);
            viewBtn.setStyle("-fx-background-color:#1a2a4a;-fx-text-fill:white;" +
                    "-fx-font-size:11px;-fx-background-radius:8px;-fx-cursor:hand;");
            viewBtn.setOnMouseEntered(e -> viewBtn.setStyle(
                    "-fx-background-color:#6a9ae7;-fx-text-fill:white;" +
                            "-fx-font-size:11px;-fx-background-radius:8px;-fx-cursor:hand;"));
            viewBtn.setOnMouseExited(e -> viewBtn.setStyle(
                    "-fx-background-color:#1a2a4a;-fx-text-fill:white;" +
                            "-fx-font-size:11px;-fx-background-radius:8px;-fx-cursor:hand;"));

            final int rid = resultId;
            final String uname = username;
            viewBtn.setOnAction(e -> openResultReview(rid, uname));
            row.getChildren().add(viewBtn);
        }

        return row;
    }

    private void openResultReview(int resultId, String viewedUsername) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/ResultReview.fxml"));

            // 1. FIRST: Load the FXML. This initializes all @FXML fields like titleLabel.
            Parent root = loader.load();

            // 2. SECOND: Get the controller AFTER loading is complete.
            ResultReviewController rc = loader.getController();

            // 3. THIRD: Now it is safe to call your method.
            rc.setResultInfo(resultId, isTeacher,
                    currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail,
                    currentRole, viewedUsername);

            // 4. FINALLY: Switch the scene.
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPlaceholder() {
        leaderboardContainer.getChildren().clear();
        Label msg = new Label("👆  Select an exam above to see the leaderboard");
        msg.setStyle("-fx-text-fill:#aaa;-fx-font-size:14px;" +
                "-fx-font-style:italic;-fx-padding:40px 0 0 200px;");
        leaderboardContainer.getChildren().add(msg);
    }

    private void showNoResults() {
        leaderboardContainer.getChildren().clear();
        Label msg = new Label("No results yet for this exam.");
        msg.setStyle("-fx-text-fill:#aaa;-fx-font-size:14px;" +
                "-fx-font-style:italic;-fx-padding:40px 0 0 250px;");
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
                        "/com/buet/exam_system/TeacherDashboard.fxml"));
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