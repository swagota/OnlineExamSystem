package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerformanceController {

    private String currentUsername    = "";
    private String currentEmail       = "";
    private String currentFatherEmail = "";
    private String currentMotherEmail = "";
    private int    currentRole        = 2;

    @FXML private Button backBtn;
    @FXML private ComboBox<String> filterCombo;

    @FXML private Label totalExamsCard;
    @FXML private Label avgScoreCard;
    @FXML private Label bestScoreCard;
    @FXML private Label passRateCard;

    @FXML private BarChart<String, Number> scoreBarChart;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis barYAxis;

    @FXML private VBox recentResultsBox;

    public void setStudentInfo(String username, String email, String fatherEmail,
                               String motherEmail, int role) {
        this.currentUsername    = username    != null ? username    : "";
        this.currentEmail       = email       != null ? email       : "";
        this.currentFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.currentMotherEmail = motherEmail != null ? motherEmail : "";
        this.currentRole        = role;

        loadFilters();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        String currentMonthName = java.time.Month.of(currentMonth).name().charAt(0)
                + java.time.Month.of(currentMonth).name().substring(1).toLowerCase();
        loadStats(currentMonthName);
        loadBarChart(currentMonthName);
        loadRecentResults();
    }

    private void loadFilters() {
        filterCombo.getItems().clear();
        filterCombo.getItems().addAll("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        filterCombo.setValue(java.time.Month.of(java.time.LocalDate.now().getMonthValue()).name()
                .substring(0,1) + java.time.Month.of(java.time.LocalDate.now().getMonthValue())
                .name().substring(1).toLowerCase());
    }

    @FXML
    private void handleFilter() {
        String filter = filterCombo.getValue();
        if (filter == null) return;
        loadStats(filter);
        loadBarChart(filter);
    }

    private String getDateFilter(String filter) {
        int monthNum = java.time.Month.valueOf(filter.toUpperCase()).getValue();
        return " AND MONTH(submitted_at) = " + monthNum;
    }

    private void loadStats(String filter) {
        String dateFilter = getDateFilter(filter);
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            String sql = "SELECT COUNT(*) as total, " +
                    "AVG(score*100.0/total) as avg_score, " +
                    "MAX(score*100.0/total) as best_score, " +
                    "SUM(CASE WHEN score*100.0/total >= 50 THEN 1 ELSE 0 END) as passed " +
                    "FROM results WHERE username = ?" + dateFilter;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, currentUsername);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int total    = rs.getInt("total");
                double avg   = rs.getDouble("avg_score");
                double best  = rs.getDouble("best_score");
                int passed   = rs.getInt("passed");

                totalExamsCard.setText(String.valueOf(total));
                avgScoreCard.setText(String.format("%.0f%%", avg));
                bestScoreCard.setText(String.format("%.0f%%", best));
                passRateCard.setText(total > 0 ?
                        String.format("%.0f%%", (passed * 100.0 / total)) : "0%");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBarChart(String filter) {
        scoreBarChart.getData().clear();
        String dateFilter = getDateFilter(filter);

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            String sql = "SELECT exam_name, MAX(score*100.0/total) as best_pct " +
                    "FROM results WHERE username = ?" + dateFilter +
                    " GROUP BY exam_name ORDER BY submitted_at DESC LIMIT 8";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, currentUsername);
            ResultSet rs = ps.executeQuery();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Score %");

            while (rs.next()) {
                String examName = rs.getString("exam_name");
                double pct      = rs.getDouble("best_pct");
                // Shorten long exam names
                if (examName.length() > 12) examName = examName.substring(0, 12) + "...";
                series.getData().add(new XYChart.Data<>(examName, pct));
            }

            scoreBarChart.getData().add(series);

            // Color bars based on score
            for (XYChart.Data<String, Number> data : series.getData()) {
                double val = data.getYValue().doubleValue();
                String color = val >= 80 ? "#2ecc71" : val >= 50 ? "#f39c12" : "#e74c3c";
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentResults() {
        recentResultsBox.getChildren().clear();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT exam_name, score, total, submitted_at FROM results " +
                            "WHERE username = ? ORDER BY submitted_at DESC LIMIT 6");
            ps.setString(1, currentUsername);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String examName = rs.getString("exam_name");
                int score       = rs.getInt("score");
                int total       = rs.getInt("total");
                String date     = rs.getString("submitted_at").substring(0, 10);
                double pct      = total > 0 ? score * 100.0 / total : 0;
                boolean passed  = pct >= 50;

                // Build result row
                AnchorPane row = new AnchorPane();
                row.setPrefHeight(40);
                row.setPrefWidth(280);
                String rowBg = passed ? "#f0fff4" : "#fff0f0";
                row.setStyle("-fx-background-color: " + rowBg + "; -fx-background-radius: 8px; -fx-padding: 5;");

                String shortName = examName.length() > 16 ? examName.substring(0, 16) + "..." : examName;
                Label nameLabel = new Label(shortName);
                nameLabel.setLayoutX(8);
                nameLabel.setLayoutY(4);
                nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a2a4a;");

                Label dateLabel = new Label(date);
                dateLabel.setLayoutX(8);
                dateLabel.setLayoutY(22);
                dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

                String scoreColor = passed ? "#2ecc71" : "#e74c3c";
                Label scoreLabel = new Label(String.format("%.0f%%", pct));
                scoreLabel.setLayoutX(220);
                scoreLabel.setLayoutY(10);
                scoreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + scoreColor + ";");

                row.getChildren().addAll(nameLabel, dateLabel, scoreLabel);
                recentResultsBox.getChildren().add(row);
            }

            if (recentResultsBox.getChildren().isEmpty()) {
                Label empty = new Label("No results yet. Take an exam!");
                empty.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
                recentResultsBox.getChildren().add(empty);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/StudentDashboard.fxml"));
            Parent root = loader.load();
            StudentDashboardController sdc = loader.getController();
            sdc.setStudentInfo(currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail, currentRole);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
