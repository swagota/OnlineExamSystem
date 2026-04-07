package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

public class DiscussionController {

    @FXML private Button backBtn;
    @FXML private Button submitBtn;
    @FXML private Button searchBtn;
    @FXML private Button clearBtn;
    @FXML private TextField searchField;
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
            leftPanelSubtitle.setText("Answer the questions of students");
            //roleLabel.setText("Logged in as: Teacher");
        } else {
           // roleLabel.setText("Logged in as: Student");
        }

        loadAllDiscussions("");
        if (!isTeacher) loadMyQuestions();
    }


    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        loadAllDiscussions(query);
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadAllDiscussions("");
    }

    private void loadAllDiscussions(String searchQuery) {
        allDiscussionsBox.getChildren().clear();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            String sql;
            PreparedStatement ps;

            if (searchQuery == null || searchQuery.isBlank()) {
                sql = "SELECT * FROM discussions ORDER BY created_at DESC";
                ps = conn.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM discussions WHERE question LIKE ? ORDER BY created_at DESC";
                ps = conn.prepareStatement(sql);
                ps.setString(1, "%" + searchQuery + "%");
            }

            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                int    id        = rs.getInt("id");
                String asker     = rs.getString("username");
                String question  = rs.getString("question");
                String createdAt = rs.getString("created_at").substring(0, 16);
                allDiscussionsBox.getChildren().add(
                        createDiscussionCard(id, asker, question, createdAt));
            }
            if (!any) {
                String msg = searchQuery.isBlank()
                        ? "No questions yet. Be the first to ask!"
                        : "No questions found for \"" + searchQuery + "\"";
                Label empty = new Label(msg);
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
                    "SELECT d.id, d.question, COUNT(r.id) as reply_count " +
                            "FROM discussions d " +
                            "LEFT JOIN discussion_replies r ON d.id = r.discussion_id " +
                            "WHERE d.username = ? " +
                            "GROUP BY d.id, d.question " +
                            "ORDER BY d.created_at DESC");
            ps.setString(1, currentUsername);
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                any = true;
                String q         = rs.getString("question");
                int replyCount   = rs.getInt("reply_count");
                boolean answered = replyCount > 0;

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

                String status = answered ? replyCount + " reply" : "Pending";
                Label statusLabel = new Label(status);
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

    private VBox createDiscussionCard(int id, String asker,
                                      String question, String createdAt) {
        VBox card = new VBox(8);
        card.setPrefWidth(580);
        card.setPadding(new Insets(14, 16, 14, 16));

        int replyCount   = getReplyCount(id);
        boolean hasReplies = replyCount > 0;

        card.setStyle("-fx-background-color: " + (hasReplies ? "#f0fff8" : "white") +
                "; -fx-background-radius: 12px; -fx-border-color: " +
                (hasReplies ? "#a8e6c0" : "#dce8ff") +
                "; -fx-border-radius: 12px; -fx-border-width: 1px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);");

        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label avatarLabel = new Label(asker.substring(0, 1).toUpperCase());
        avatarLabel.setPrefWidth(32); avatarLabel.setPrefHeight(32);
        avatarLabel.setStyle("-fx-background-color: #6a9ae7; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 14px;" +
                "-fx-background-radius: 50%; -fx-alignment: center;");

        Label nameLabel = new Label(asker);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2a4a;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label timeLabel = new Label(createdAt);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

        String badgeText = hasReplies ? replyCount + " Replies" : "No Replies";
        String badgeBg   = hasReplies ? "#27ae60" : "#e67e22";
        Label statusBadge = new Label(badgeText);
        statusBadge.setStyle("-fx-background-color: " + badgeBg +
                "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-background-radius: 10px; -fx-padding: 2 8 2 8;");

        header.getChildren().addAll(avatarLabel, nameLabel, timeLabel, statusBadge);

        Label qLabel = new Label(question);
        qLabel.setWrapText(true);
        qLabel.setMaxWidth(555);
        qLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #222; -fx-padding: 2 0 0 0;");

        card.getChildren().addAll(header, qLabel);

        loadReplies(id, card);

        Button replyBtn = new Button("Reply");
        replyBtn.setStyle("-fx-background-color: #1a2a4a; -fx-text-fill: white; -fx-font-size: 12px;" +
                "-fx-background-radius: 8px; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        replyBtn.setOnMouseEntered(e -> replyBtn.setStyle(
                "-fx-background-color: #6a9ae7; -fx-text-fill: white; -fx-font-size: 12px;" +
                        "-fx-background-radius: 8px; -fx-padding: 6 16 6 16; -fx-cursor: hand;"));
        replyBtn.setOnMouseExited(e -> replyBtn.setStyle(
                "-fx-background-color: #1a2a4a; -fx-text-fill: white; -fx-font-size: 12px;" +
                        "-fx-background-radius: 8px; -fx-padding: 6 16 6 16; -fx-cursor: hand;"));
        replyBtn.setOnAction(e -> showReplyDialog(id));
        card.getChildren().add(replyBtn);

        return card;
    }

    private void loadReplies(int discussionId, VBox card) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT username, role, reply, created_at FROM discussion_replies " +
                            "WHERE discussion_id = ? ORDER BY created_at ASC");
            ps.setInt(1, discussionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String replier   = rs.getString("username");
                int    role      = rs.getInt("role");
                String replyText = rs.getString("reply");
                String time      = rs.getString("created_at").substring(0, 16);
                boolean isT      = (role == 1);

                VBox replyBox = new VBox(4);
                replyBox.setStyle("-fx-background-color: " + (isT ? "#e8f8f0" : "#eef4ff") +
                        "; -fx-background-radius: 8px; -fx-padding: 8;");

                String roleColor = isT ? "#27ae60" : "#6a9ae7";
                Label replyHeader = new Label(replier + "  [" + (isT ? "Teacher" : "Student") + "]  " + time);
                replyHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + roleColor + ";");

                Label replyLabel = new Label(replyText);
                replyLabel.setWrapText(true);
                replyLabel.setMaxWidth(530);
                replyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a2a4a;");

                replyBox.getChildren().addAll(replyHeader, replyLabel);
                card.getChildren().add(replyBox);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int getReplyCount(int discussionId) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM discussion_replies WHERE discussion_id = ?");
            ps.setInt(1, discussionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
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
                    "INSERT INTO discussions (username, role, question) VALUES (?, ?, ?)");
            ps.setString(1, currentUsername);
            ps.setInt(2, currentRole);
            ps.setString(3, text);
            ps.executeUpdate();
            questionInput.clear();
            loadAllDiscussions(searchField.getText().trim());
            loadMyQuestions();
            new Alert(Alert.AlertType.INFORMATION, "Question submitted successfully!").showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showReplyDialog(int discussionId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Reply");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4ff;");
        root.setPrefWidth(500);

        Label title = new Label("Write your reply:");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a2a4a;");

        TextArea replyField = new TextArea();
        replyField.setPromptText("Type your reply here...");
        replyField.setPrefRowCount(5);
        replyField.setWrapText(true);

        HBox btnRow = new HBox(10);
        Button saveBtn = new Button("Submit Reply");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #bbb; -fx-text-fill: white;" +
                "-fx-background-radius: 8px; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String reply = replyField.getText().trim();
            if (reply.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please write a reply!").showAndWait();
                return;
            }
            saveReply(discussionId, reply);
            dialog.close();
        });

        btnRow.getChildren().addAll(saveBtn, cancelBtn);
        root.getChildren().addAll(title, replyField, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void saveReply(int discussionId, String reply) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO discussion_replies (discussion_id, username, role, reply) VALUES (?, ?, ?, ?)");
            ps.setInt(1, discussionId);
            ps.setString(2, currentUsername);
            ps.setInt(3, currentRole);
            ps.setString(4, reply);
            ps.executeUpdate();
            loadAllDiscussions(searchField.getText().trim());
            if (!isTeacher) loadMyQuestions();
            new Alert(Alert.AlertType.INFORMATION, "Reply submitted!").showAndWait();
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