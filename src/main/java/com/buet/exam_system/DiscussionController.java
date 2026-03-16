package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Statement;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import javafx.application.Platform;

import java.sql.*;

public class DiscussionController {

    @FXML private Button backBtn;
    @FXML private Button submitBtn;
    @FXML private TextArea questionInput;
    @FXML private VBox myQuestionsBox;
    @FXML private VBox allDiscussionsBox;
    @FXML private Label roleLabel;
    @FXML private Label myQuestionsTitle;
    @FXML private ScrollPane myQuestionsScroll;
    @FXML private Label leftPanelTitle;
    @FXML private Label leftPanelSubtitle;

    private String currentUsername    = "";
    private String currentEmail       = "";
    private String currentFatherEmail = "";
    private String currentMotherEmail = "";
    private int    currentRole        = 2;
    private boolean isTeacher         = false;
    private static final String GEMINI_API_KEY = "put yours";

    public void setUserInfo(String username, String email,
                            String fatherEmail, String motherEmail, int role) {
        this.currentUsername    = username    != null ? username    : "";
        this.currentEmail       = email       != null ? email       : "";
        this.currentFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.currentMotherEmail = motherEmail != null ? motherEmail : "";
        this.currentRole        = role;
        this.isTeacher          = (role == 1);

        if (isTeacher) {
            questionInput.setVisible(false);
            questionInput.setManaged(false);
            submitBtn.setVisible(false);
            submitBtn.setManaged(false);
            myQuestionsTitle.setVisible(false);
            myQuestionsTitle.setManaged(false);
            myQuestionsScroll.setVisible(false);
            myQuestionsScroll.setManaged(false);
            leftPanelTitle.setText("Student's Questions");
            leftPanelSubtitle.setText("Answer the questions of student");
            roleLabel.setText("Logged in as: Teacher");
        } else {
            roleLabel.setText("Logged in as: Student");
        }

        loadAllDiscussions();
        if (!isTeacher) loadMyQuestions();
        checkOldUnansweredQuestions();
    }

    private void loadAllDiscussions() {
        allDiscussionsBox.getChildren().clear();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM discussions ORDER BY created_at DESC");
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                int    id         = rs.getInt("id");
                String asker      = rs.getString("username");
                String question   = rs.getString("question");
                String answer     = rs.getString("answer");
                String answeredBy = rs.getString("answered_by");
                String createdAt  = rs.getString("created_at").substring(0, 16);
                allDiscussionsBox.getChildren().add(
                        createDiscussionCard(id, asker, question, answer, answeredBy, createdAt));
            }
            if (!any) {
                Label empty = new Label("No questions yet. Be the first to ask!");
                empty.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px; -fx-font-style: italic; -fx-padding: 20px;");
                allDiscussionsBox.getChildren().add(empty);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMyQuestions() {
        myQuestionsBox.getChildren().clear();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, question, answer FROM discussions WHERE username = ? ORDER BY created_at DESC");
            ps.setString(1, currentUsername);
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                String q         = rs.getString("question");
                String ans       = rs.getString("answer");
                boolean answered = ans != null && !ans.isEmpty();

                AnchorPane mini = new AnchorPane();
                mini.setPrefWidth(240);
                mini.setMinHeight(55);
                mini.setStyle("-fx-background-color: " + (answered ? "#e8f8f0" : "#fff8e8") +
                        "; -fx-background-radius: 8px; -fx-padding: 8;");

                String shortQ = q.length() > 40 ? q.substring(0, 40) + "..." : q;
                Label qLabel = new Label(shortQ);
                qLabel.setLayoutX(8); qLabel.setLayoutY(5);
                qLabel.setPrefWidth(220);
                qLabel.setWrapText(true);
                qLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1a2a4a; -fx-font-weight: bold;");

                Label statusLabel = new Label(answered ? "Answered" : "Pending");
                statusLabel.setLayoutX(8); statusLabel.setLayoutY(38);
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " +
                        (answered ? "#27ae60" : "#e67e22") + "; -fx-font-weight: bold;");

                mini.getChildren().addAll(qLabel, statusLabel);
                myQuestionsBox.getChildren().add(mini);
            }
            if (!any) {
                Label empty = new Label("No questions yet.");
                empty.setStyle("-fx-text-fill: #aac4e8; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 10px;");
                myQuestionsBox.getChildren().add(empty);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox createDiscussionCard(int id, String asker, String question,
                                      String answer, String answeredBy, String createdAt) {
        VBox card = new VBox(8);
        card.setPrefWidth(580);
        card.setPadding(new Insets(14, 16, 14, 16));
        boolean answered = answer != null && !answer.isEmpty();
        card.setStyle("-fx-background-color: " + (answered ? "#f0fff8" : "white") +
                "; -fx-background-radius: 12px; -fx-border-color: " +
                (answered ? "#a8e6c0" : "#dce8ff") +
                "; -fx-border-radius: 12px; -fx-border-width: 1px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");

        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label avatarLabel = new Label(asker.substring(0, 1).toUpperCase());
        avatarLabel.setPrefWidth(32); avatarLabel.setPrefHeight(32);
        avatarLabel.setStyle("-fx-background-color: #6a9ae7; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-font-size: 14px; -fx-background-radius: 50%; -fx-alignment: center;");

        Label nameLabel = new Label(asker);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2a4a;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label timeLabel = new Label(createdAt);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

        Label statusBadge = new Label(answered ? "Answered" : "Unanswered");
        statusBadge.setStyle("-fx-background-color: " + (answered ? "#27ae60" : "#e67e22") +
                "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10px; -fx-padding: 2 8 2 8;");

        header.getChildren().addAll(avatarLabel, nameLabel, timeLabel, statusBadge);

        Label qLabel = new Label(question);
        qLabel.setWrapText(true);
        qLabel.setMaxWidth(555);
        qLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #222; -fx-padding: 2 0 0 0;");

        card.getChildren().addAll(header, qLabel);

        if (answered) {
            VBox answerBox = new VBox(4);
            answerBox.setStyle("-fx-background-color: #e8f8f0; -fx-background-radius: 8px; -fx-padding: 10;");
            Label answerTitle = new Label("Answered by " + (answeredBy != null ? answeredBy : "Teacher") + ":");
            answerTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            Label answerText = new Label(answer);
            answerText.setWrapText(true);
            answerText.setMaxWidth(535);
            answerText.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a2a4a;");
            answerBox.getChildren().addAll(answerTitle, answerText);
            card.getChildren().add(answerBox);
        }

        if (isTeacher && !answered) {
            Button answerBtn = new Button("Answer this question");
            answerBtn.setStyle("-fx-background-color: #1a2a4a; -fx-text-fill: white; -fx-font-size: 12px;" +
                    "-fx-background-radius: 8px; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
            answerBtn.setOnMouseEntered(e -> answerBtn.setStyle(
                    "-fx-background-color: #6a9ae7; -fx-text-fill: white; -fx-font-size: 12px;" +
                            "-fx-background-radius: 8px; -fx-padding: 6 16 6 16; -fx-cursor: hand;"));
            answerBtn.setOnMouseExited(e -> answerBtn.setStyle(
                    "-fx-background-color: #1a2a4a; -fx-text-fill: white; -fx-font-size: 12px;" +
                            "-fx-background-radius: 8px; -fx-padding: 6 16 6 16; -fx-cursor: hand;"));
            answerBtn.setOnAction(e -> showAnswerDialog(id));
            card.getChildren().add(answerBtn);
        }

        return card;
    }

    private void checkAndReplyIfUnanswered(int questionId) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT question, answer FROM discussions WHERE id = ?");
            ps.setInt(1, questionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && (rs.getString("answer") == null || rs.getString("answer").isEmpty())) {
                String aiAnswer = getGeminiAnswer(rs.getString("question"));
                if (aiAnswer != null) saveAiAnswer(questionId, aiAnswer, conn);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getGeminiAnswer(String question) {
        try {
            String url = "https://api.groq.com/openai/v1/chat/completions";
            String body = "{\"model\":\"llama-3.3-70b-versatile\",\"messages\":[{\"role\":\"user\",\"content\":\"" +
                    question.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GEMINI_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String resp = response.body();
            System.out.println("Groq Response: " + response.body());
            int start = resp.indexOf("\"content\":\"") + 11;
            int end = resp.indexOf("\"", start);
            if (start > 11 && end > start) {
                return resp.substring(start, end).replace("\\n", "\n");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void saveAiAnswer(int id, String answer, Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE discussions SET answer = ?, answered_by = ? WHERE id = ?");
            ps.setString(1, answer);
            ps.setString(2, "Groq AI");
            ps.setInt(3, id);
            ps.executeUpdate();
            Platform.runLater(() -> loadAllDiscussions());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void checkOldUnansweredQuestions() {
        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin", "root", "")) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, question FROM discussions " +
                                "WHERE answer IS NULL AND created_at <= NOW() - INTERVAL 1 MINUTE");
                ResultSet rs = ps.executeQuery();
                //boolean found = false;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String question = rs.getString("question");
                    //System.out.println("Found unanswered: " + id + " - " + question);
                    String aiAnswer = getGeminiAnswer(question);
                    //System.out.println("Gemini said: " + aiAnswer);
                    if (aiAnswer != null) saveAiAnswer(id, aiAnswer, conn);
                }
                //if (!found) System.out.println("No old unanswered questions found");
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void handleSubmit() {
        String text = questionInput.getText().trim();
        if (text.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please write a question first!").showAndWait();
            return;
        }
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO discussions (username, role, question) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, currentUsername);
            ps.setInt(2, currentRole);
            ps.setString(3, text);
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            int questionId = 0;
            if (generatedKeys.next()) questionId = generatedKeys.getInt(1);
            final int finalId = questionId;
            new Thread(() -> {
                try {
                    Thread.sleep( 60 * 1000);
                    checkAndReplyIfUnanswered(finalId);
                } catch (InterruptedException e) { e.printStackTrace(); }
            }).start();
            questionInput.clear();
            loadAllDiscussions();
            loadMyQuestions();
            new Alert(Alert.AlertType.INFORMATION, "Question submitted successfully!").showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAnswerDialog(int discussionId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Answer Question");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4ff;");
        root.setPrefWidth(500);

        Label title = new Label("Write your answer:");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a2a4a;");

        TextArea answerField = new TextArea();
        answerField.setPromptText("Type your answer here...");
        answerField.setPrefRowCount(5);
        answerField.setWrapText(true);

        HBox btnRow = new HBox(10);
        Button saveBtn = new Button("Submit Answer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8px; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #bbb; -fx-text-fill: white;" +
                "-fx-background-radius: 8px; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String ans = answerField.getText().trim();
            if (ans.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please write an answer!").showAndWait();
                return;
            }
            saveAnswer(discussionId, ans);
            dialog.close();
        });

        btnRow.getChildren().addAll(saveBtn, cancelBtn);
        root.getChildren().addAll(title, answerField, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void saveAnswer(int id, String answer) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE discussions SET answer = ?, answered_by = ? WHERE id = ?");
            ps.setString(1, answer);
            ps.setString(2, currentUsername);
            ps.setInt(3, id);
            ps.executeUpdate();
            loadAllDiscussions();
            new Alert(Alert.AlertType.INFORMATION, "Answer submitted successfully!").showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
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
