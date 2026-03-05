package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ViewQuestionsController implements Initializable {

    @FXML private Button backBtn;
    @FXML private VBox questionsContainer;
    @FXML private ScrollPane scrollPane;

    private int examId;

    private String teacherUsername    = "";
    private String teacherEmail       = "";
    private String teacherFatherEmail = "";
    private String teacherMotherEmail = "";

    public void setExamId(int examId) {
        this.examId = examId;
        loadQuestions();
    }

    public void setTeacherInfo(String username, String email,
                               String fatherEmail, String motherEmail) {
        this.teacherUsername = username != null ? username : "";
        this.teacherEmail = email != null ? email : "";
        this.teacherFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.teacherMotherEmail = motherEmail != null ? motherEmail : "";
    }

        @FXML
        private void handleBack() {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/buet/exam_system/SubjectPage.fxml"));
                Parent root = loader.load();

                SubjectPageController spc = loader.getController();
                spc.setUserInfo(false, teacherUsername, teacherEmail,
                        teacherFatherEmail, teacherMotherEmail, 1);

                Stage stage = (Stage) backBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

            @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    private void loadQuestions() {
        try {
            Connection connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin", "root", "");

            String sql = "SELECT * FROM questions WHERE exam_id=?";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();

            questionsContainer.getChildren().clear();
            int serial = 1;

            while (rs.next()) {
                String questionText = rs.getString("question");
                String[] options = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };
                int correctAnswer = rs.getInt("correct_answer");
                questionsContainer.getChildren().add(
                        createQuestionCard(serial, questionText, options, correctAnswer));
                serial++;
            }

            if (serial == 1) {
                Label empty = new Label("No questions found for this exam.");
                empty.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px; " +
                        "-fx-font-style: italic; -fx-padding: 30px;");
                questionsContainer.getChildren().add(empty);
            }

            rs.close(); ps.close(); connect.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createQuestionCard(int serial, String questionText, String[] options, int correctAnswer) {

        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: #dce8ff;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);"
        );
        card.setPadding(new Insets(16, 20, 16, 20));

        HBox questionHeader = new HBox(10);
        questionHeader.setPadding(new Insets(0, 0, 6, 0));

        Label badge = new Label("Q" + serial);
        badge.setStyle(
                "-fx-background-color: #1a2a4a;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 3px 9px;"
        );

        Label questionLabel = new Label(questionText);
        questionLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1a2a4a;" +
                        "-fx-wrap-text: true;"
        );
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(680);

        questionHeader.getChildren().addAll(badge, questionLabel);
        card.getChildren().add(questionHeader);

        Label divider = new Label();
        divider.setPrefWidth(740);
        divider.setStyle("-fx-border-color: #eef2ff; -fx-border-width: 1px 0 0 0; -fx-padding: 0;");
        card.getChildren().add(divider);
        String[] optionLabels = {"A", "B", "C", "D"};
        ToggleGroup group = new ToggleGroup();

        for (int i = 0; i < options.length; i++) {
            boolean isCorrect = (i + 1) == correctAnswer;

            HBox optionBox = new HBox(10);
            optionBox.setPadding(new Insets(4, 10, 4, 10));
            Label letterBadge = new Label(optionLabels[i]);
            letterBadge.setMinWidth(24);
            letterBadge.setMinHeight(24);

            if (isCorrect) {
                letterBadge.setStyle(
                        "-fx-background-color: #27ae60;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 50%;" +
                                "-fx-alignment: center;" +
                                "-fx-padding: 3px 7px;"
                );
            } else {
                letterBadge.setStyle(
                        "-fx-background-color: #6a9ae7;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 50%;" +
                                "-fx-alignment: center;" +
                                "-fx-padding: 3px 7px;"
                );
            }

            Label optionText = new Label(options[i]);
            optionText.setWrapText(true);
            optionText.setMaxWidth(640);

            if (isCorrect) {
                optionBox.setStyle(
                        "-fx-background-color: #e8f8f0;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-border-color: #a8e6c0;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-border-width: 1px;"
                );
                optionText.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-text-fill: #1e8449;" +
                                "-fx-font-weight: bold;"
                );
            } else {
                optionBox.setStyle(
                        "-fx-background-color: #f8faff;" +
                                "-fx-background-radius: 8px;"
                );
                optionText.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-text-fill: #444444;"
                );
            }

            optionBox.getChildren().addAll(letterBadge, optionText);
            card.getChildren().add(optionBox);
        }

        return card;
    }
}