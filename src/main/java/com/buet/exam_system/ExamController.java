package com.buet.exam_system;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExamController implements Initializable {

    @FXML private Label questionLabel;
    @FXML private Label timerLabel;
    @FXML private RadioButton option1;
    @FXML private RadioButton option2;
    @FXML private RadioButton option3;
    @FXML private RadioButton option4;
    @FXML private Button nextBtn;
    @FXML private Button submitBtn;

    private ToggleGroup optionsGroup;

    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    private int totalSeconds = 0;   // set via setExamId()
    private Timeline countdown;

    private int examId;
    private Connection connect;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        optionsGroup = new ToggleGroup();
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);
    }
    public void setExamId(int examId) {
        this.examId = examId;
        loadExamTime();
        loadQuestionsFromDatabase();
        if (!questions.isEmpty()) {
            loadQuestion();
        }
        startTimer();
    }

    private void loadExamTime() {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            String sql = "SELECT total_time FROM exams WHERE id = ?";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalSeconds = rs.getInt("total_time") * 60; // stored as minutes
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        updateTimerLabel();
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            totalSeconds--;
            updateTimerLabel();
            if (totalSeconds <= 0) {
                countdown.stop();
                checkAnswer();
                showResult();
            }
        }));
        countdown.setCycleCount(Animation.INDEFINITE);
        countdown.play();
    }

    private void updateTimerLabel() {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timerLabel.setText(String.format("⏱ %02d:%02d", minutes, seconds));
        // Turn red in last 30 seconds
        if (totalSeconds <= 30) {
            timerLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void loadQuestionsFromDatabase() {

        try {
            connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin",
                    "root",
                    ""
            );

            String sql = "SELECT * FROM questions WHERE exam_id = ?";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String questionText = rs.getString("question");

                String[] options = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };

                int correctIndex = rs.getInt("correct_answer") - 1;

                questions.add(new Question(questionText, options, correctIndex));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadQuestion() {

        Question q = questions.get(currentQuestionIndex);
        int qNum = currentQuestionIndex + 1;
        questionLabel.setText("Q" + qNum + ". " + q.getQuestion());
        option1.setText(q.getOptions()[0]);
        option2.setText(q.getOptions()[1]);
        option3.setText(q.getOptions()[2]);
        option4.setText(q.getOptions()[3]);

        optionsGroup.selectToggle(null);
    }

    @FXML
    private void handleNext() {

        checkAnswer();

        currentQuestionIndex++;

        if (currentQuestionIndex < questions.size()) {
            loadQuestion();
        } else {
            if (countdown != null) countdown.stop();
            showResult();
        }
    }

    @FXML
    private void handleSubmit() {
        checkAnswer();
        if (countdown != null) countdown.stop();
        showResult();
    }

    private void checkAnswer() {

        RadioButton selected =
                (RadioButton) optionsGroup.getSelectedToggle();

        if (selected == null) return;

        int selectedIndex = -1;

        if (selected == option1) selectedIndex = 0;
        if (selected == option2) selectedIndex = 1;
        if (selected == option3) selectedIndex = 2;
        if (selected == option4) selectedIndex = 3;

        if (selectedIndex ==
                questions.get(currentQuestionIndex).getCorrectIndex()) {
            score++;
        }
    }

    private void showResult() {

        questionLabel.setText("✅Exam Finished! Your Score: "
                + score + " / " + questions.size());
        timerLabel.setText("Time's up!");
        option1.setDisable(true);
        option2.setDisable(true);
        option3.setDisable(true);
        option4.setDisable(true);

        nextBtn.setDisable(true);
        submitBtn.setDisable(true);
    }
}