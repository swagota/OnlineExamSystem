package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ViewQuestionsController implements Initializable {

    @FXML private Button backBtn;
    @FXML private VBox questionsContainer;
    @FXML private ScrollPane scrollPane;

    private int examId;

    public void setExamId(int examId) {
        this.examId = examId;
        loadQuestions();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/SubjectPage.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(scene);
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
                int correctAnswer = rs.getInt("correct_answer"); // 1-based

                // Create question card
                VBox card = createQuestionCard(serial, questionText, options, correctAnswer);
                questionsContainer.getChildren().add(card);
                serial++;
            }

            rs.close();
            ps.close();
            connect.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createQuestionCard(int serial, String questionText, String[] options, int correctAnswer) {

        // Card container
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );
        card.setPadding(new Insets(16, 20, 16, 20));

        // Question label
        Label questionLabel = new Label("Q" + serial + ".  " + questionText);
        questionLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-wrap-text: true;"
        );
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(680);

        card.getChildren().add(questionLabel);

        // Options as RadioButtons (non-interactive, just for display)
        ToggleGroup group = new ToggleGroup();
        String[] labels = {"A", "B", "C", "D"};

        for (int i = 0; i < options.length; i++) {
            HBox optionBox = new HBox(10);
            optionBox.setPadding(new Insets(4, 0, 4, 10));

            RadioButton rb = new RadioButton(labels[i] + ".   " + options[i]);
            rb.setToggleGroup(group);
            rb.setMouseTransparent(true); // non-clickable — view only
            rb.setFocusTraversable(false);

            boolean isCorrect = (i + 1) == correctAnswer;

            if (isCorrect) {
                // Correct answer — green highlight
                rb.setSelected(true);
                optionBox.setStyle(
                        "-fx-background-color: #e8f8f0;" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 6 10 6 10;"
                );
                rb.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-text-fill: #27ae60;" +
                                "-fx-font-weight: bold;"
                );
            } else {
                optionBox.setStyle("-fx-padding: 6 10 6 10;");
                rb.setStyle(
                        "-fx-font-size: 13px;" +
                                "-fx-text-fill: #555555;"
                );
            }

            optionBox.getChildren().add(rb);
            card.getChildren().add(optionBox);
        }

        return card;
    }
}