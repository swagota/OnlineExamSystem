package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ResultReviewController implements Initializable {

    @FXML private Button backBtn;
    @FXML private VBox questionsContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private Label titleLabel;
    @FXML private Label scoreLabel;

    private int resultId;
    private boolean isTeacher         = false;
    private String  currentUsername    = "";
    private String  currentEmail       = "";
    private String  currentFatherEmail = "";
    private String  currentMotherEmail = "";
    private int     currentRole        = 2;
    private String  viewedUsername     = ""; // whose result we are viewing

    public void setResultInfo(int resultId, boolean isTeacher,
                              String username, String email,
                              String fatherEmail, String motherEmail, int role,
                              String viewedUsername) {
        this.resultId          = resultId;
        this.isTeacher         = isTeacher;
        this.currentUsername   = username    != null ? username    : "";
        this.currentEmail      = email       != null ? email       : "";
        this.currentFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.currentMotherEmail = motherEmail != null ? motherEmail : "";
        this.currentRole       = role;
        this.viewedUsername    = viewedUsername != null ? viewedUsername : "";

        titleLabel.setText("📋  Answer Review — " + this.viewedUsername);
        loadReview();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    private void loadReview() {
        questionsContainer.getChildren().clear();

        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            // Load score summary
            PreparedStatement rps = connect.prepareStatement(
                    "SELECT score, total, exam_name FROM results WHERE id = ?");
            rps.setInt(1, resultId);
            ResultSet rrs = rps.executeQuery();
            if (rrs.next()) {
                double score = rrs.getDouble("score");
                int total    = rrs.getInt("total");
                String exam  = rrs.getString("exam_name");
                scoreLabel.setText(exam + "  —  Score: " + score + " / " + total);
            }

            // Load each answer
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT * FROM result_answers WHERE result_id = ? ORDER BY id ASC");
            ps.setInt(1, resultId);
            ResultSet rs = ps.executeQuery();

            int serial = 1;
            while (rs.next()) {
                String qText    = rs.getString("question_text");
                String[] opts   = {
                        rs.getString("option1"), rs.getString("option2"),
                        rs.getString("option3"), rs.getString("option4")
                };
                int selected    = rs.getInt("selected_answer"); // 1-based, 0=skipped
                int correct     = rs.getInt("correct_answer");  // 1-based
                boolean isCorrect = rs.getBoolean("is_correct");

                questionsContainer.getChildren().add(
                        createCard(serial, qText, opts, selected, correct, isCorrect));
                serial++;
            }

            if (serial == 1) {
                Label empty = new Label("No answer data found.");
                empty.setStyle("-fx-text-fill:#aaa;-fx-font-size:14px;-fx-padding:30px;");
                questionsContainer.getChildren().add(empty);
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox createCard(int serial, String questionText, String[] options,
                            int selected, int correct, boolean isCorrect) {
        // Card border color — green if correct, red if wrong, grey if skipped
        String borderColor = selected == 0 ? "#ccc" : isCorrect ? "#27ae60" : "#e74c3c";
        String bgColor     = selected == 0 ? "#fafafa" : isCorrect ? "#f0fff8" : "#fff5f5";

        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color:" + bgColor + ";" +
                        "-fx-background-radius:12px;" +
                        "-fx-border-color:" + borderColor + ";" +
                        "-fx-border-radius:12px;" +
                        "-fx-border-width:2px;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"
        );
        card.setPadding(new Insets(16, 20, 16, 20));

        // Header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label badge = new Label("Q" + serial);
        badge.setStyle("-fx-background-color:#1a2a4a;-fx-text-fill:white;" +
                "-fx-font-size:11px;-fx-font-weight:bold;" +
                "-fx-background-radius:6px;-fx-padding:3px 9px;");

        Label qLabel = new Label(questionText);
        qLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2a4a;");
        qLabel.setWrapText(true);
        qLabel.setMaxWidth(560);
        HBox.setHgrow(qLabel, Priority.ALWAYS);

        // Result badge
        String resultText  = selected == 0 ? "⚪ Skipped" : isCorrect ? "✅ Correct" : "❌ Wrong";
        String resultColor = selected == 0 ? "#888" : isCorrect ? "#27ae60" : "#e74c3c";
        Label resultBadge  = new Label(resultText);
        resultBadge.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + resultColor + ";");

        headerRow.getChildren().addAll(badge, qLabel, resultBadge);
        card.getChildren().add(headerRow);

        // Divider
        Label divider = new Label();
        divider.setPrefWidth(740);
        divider.setStyle("-fx-border-color:#eef2ff;-fx-border-width:1px 0 0 0;");
        card.getChildren().add(divider);

        // Options
        String[] optLabels = {"A", "B", "C", "D"};
        for (int i = 0; i < options.length; i++) {
            int optNum = i + 1; // 1-based
            boolean isCorrectOpt  = (optNum == correct);
            boolean isSelectedOpt = (optNum == selected);

            HBox optBox = new HBox(10);
            optBox.setPadding(new Insets(5, 10, 5, 10));

            // Letter badge
            Label letter = new Label(optLabels[i]);
            letter.setMinWidth(24); letter.setMinHeight(24);

            String letterBg;
            if (isCorrectOpt)       letterBg = "#27ae60"; // green = correct
            else if (isSelectedOpt) letterBg = "#e74c3c"; // red = wrong selected
            else                    letterBg = "#6a9ae7"; // blue = normal

            letter.setStyle("-fx-background-color:" + letterBg + ";-fx-text-fill:white;" +
                    "-fx-font-size:11px;-fx-font-weight:bold;" +
                    "-fx-background-radius:50%;-fx-alignment:center;-fx-padding:3px 7px;");

            Label optText = new Label(options[i]);
            optText.setWrapText(true);
            optText.setMaxWidth(600);

            String optBoxBg, textColor;
            if (isCorrectOpt) {
                optBoxBg   = "-fx-background-color:#e8f8f0;-fx-background-radius:8px;" +
                        "-fx-border-color:#a8e6c0;-fx-border-radius:8px;-fx-border-width:1px;";
                textColor  = "-fx-font-size:13px;-fx-text-fill:#1e8449;-fx-font-weight:bold;";
            } else if (isSelectedOpt) {
                // Student selected wrong answer
                optBoxBg   = "-fx-background-color:#fdecea;-fx-background-radius:8px;" +
                        "-fx-border-color:#f5a9a1;-fx-border-radius:8px;-fx-border-width:1px;";
                textColor  = "-fx-font-size:13px;-fx-text-fill:#c0392b;-fx-font-weight:bold;";
            } else {
                optBoxBg   = "-fx-background-color:#f8faff;-fx-background-radius:8px;";
                textColor  = "-fx-font-size:13px;-fx-text-fill:#444444;";
            }

            optBox.setStyle(optBoxBg);
            optText.setStyle(textColor);
            optBox.getChildren().addAll(letter, optText);
            card.getChildren().add(optBox);
        }

        return card;
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
