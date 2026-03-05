package com.buet.exam_system;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
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
    private int totalSeconds = 0;
    private Timeline countdown;
    private int examId;
    private String examName = "";

    // Full student info
    private String studentUsername    = "";
    private String studentEmail       = "";
    private String studentFatherEmail = "";
    private String studentMotherEmail = "";
    private int    studentRole        = 2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        optionsGroup = new ToggleGroup();
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);
    }

    /** Keep old method for compatibility */
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

    public void setExamId(int examId) {
        this.examId = examId;
        questions.clear();
        currentQuestionIndex = 0;
        score = 0;
        optionsGroup.selectToggle(null);

        loadExamInfo();
        loadQuestionsFromDatabase();

        if (questions.isEmpty()) {
            questionLabel.setText("⚠ No questions found for this exam.");
            nextBtn.setDisable(true);
            submitBtn.setDisable(true);
            timerLabel.setText("⏱ 00:00");
            return;
        }
        loadQuestion();
        startTimer();
    }

    private void loadExamInfo() {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT total_time, exam_name FROM exams WHERE id = ?");
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalSeconds = rs.getInt("total_time") * 60;
                examName     = rs.getString("exam_name");
            }
        } catch (Exception e) { e.printStackTrace(); totalSeconds = 600; }
    }

    private void loadQuestionsFromDatabase() {
        questions.clear();
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT * FROM questions WHERE exam_id = ?");
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                questions.add(new Question(
                        rs.getString("question"),
                        new String[]{rs.getString("option1"), rs.getString("option2"),
                                rs.getString("option3"), rs.getString("option4")},
                        rs.getInt("correct_answer") - 1));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadQuestion() {
        Question q = questions.get(currentQuestionIndex);
        questionLabel.setText("Q" + (currentQuestionIndex + 1) + " of "
                + questions.size() + ":  " + q.getQuestion());
        option1.setText(q.getOptions()[0]);
        option2.setText(q.getOptions()[1]);
        option3.setText(q.getOptions()[2]);
        option4.setText(q.getOptions()[3]);
        option1.setDisable(false); option2.setDisable(false);
        option3.setDisable(false); option4.setDisable(false);
        optionsGroup.selectToggle(null);
        boolean isLast = currentQuestionIndex >= questions.size() - 1;
        nextBtn.setVisible(!isLast);
        nextBtn.setDisable(isLast);
        submitBtn.setDisable(false);
    }

    private void startTimer() {
        if (countdown != null) countdown.stop();
        updateTimerLabel();
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            totalSeconds--;
            updateTimerLabel();
            if (totalSeconds <= 0) { countdown.stop(); checkAnswer(); showResult(); }
        }));
        countdown.setCycleCount(Animation.INDEFINITE);
        countdown.play();
    }

    private void updateTimerLabel() {
        timerLabel.setText(String.format("⏱ %02d:%02d", totalSeconds / 60, totalSeconds % 60));
        timerLabel.setStyle("-fx-text-fill: " + (totalSeconds <= 30 ? "#e74c3c" : "#6a9ae7") +
                "; -fx-font-weight: bold; -fx-font-size: 22px;");
    }

    @FXML private void handleNext() {
        checkAnswer();
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) loadQuestion();
        else { if (countdown != null) countdown.stop(); showResult(); }
    }

    @FXML private void handleSubmit() {
        checkAnswer();
        if (countdown != null) countdown.stop();
        showResult();
    }

    private void checkAnswer() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) return;
        RadioButton sel = (RadioButton) optionsGroup.getSelectedToggle();
        if (sel == null) return;
        int idx = sel == option1 ? 0 : sel == option2 ? 1 : sel == option3 ? 2 : 3;
        if (idx == questions.get(currentQuestionIndex).getCorrectIndex()) score++;
    }

    private void saveResultToDb() {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            // Create table if not exists
            connect.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS results (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "username VARCHAR(100)," +
                            "exam_id INT," +
                            "exam_name VARCHAR(200)," +
                            "score INT," +
                            "total INT," +
                            "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            PreparedStatement ps = connect.prepareStatement(
                    "INSERT INTO results (username, exam_id, exam_name, score, total) VALUES (?,?,?,?,?)");
            ps.setString(1, studentUsername);
            ps.setInt(2, examId);
            ps.setString(3, examName);
            ps.setInt(4, score);
            ps.setInt(5, questions.size());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showResult() {
        saveResultToDb();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Result.fxml"));
            Parent root = loader.load();
            ResultController rc = loader.getController();
            rc.setStudentInfo(studentUsername, studentEmail,
                    studentFatherEmail, studentMotherEmail, studentRole);
            rc.setResult(score, questions.size());
            Stage stage = (Stage) submitBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}