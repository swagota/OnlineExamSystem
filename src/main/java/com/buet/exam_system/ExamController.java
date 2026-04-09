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
    private List<Question> questions    = new ArrayList<>();
    private List<Integer>  questionIds  = new ArrayList<>();  // DB id of each question
    private List<Integer>  selectedAnswers = new ArrayList<>(); // student selected (1-4), 0=skipped

    private int currentQuestionIndex = 0;
    private double score = 0;
    private int totalSeconds = 0;
    private Timeline countdown;
    private int examId;
    private String examName = "";

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

    public void setStudentInfo(String username, String email,
                               String fatherEmail, String motherEmail, int role) {
        this.studentUsername    = username    != null ? username    : "";
        this.studentEmail       = email       != null ? email       : "";
        this.studentFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.studentMotherEmail = motherEmail != null ? motherEmail : "";
        this.studentRole        = role;
    }

    public void setStudentUsername(String username) {
        this.studentUsername = username != null ? username : "";
    }

    public void setExamId(int examId) {
        this.examId = examId;
        questions.clear();
        questionIds.clear();
        selectedAnswers.clear();
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

        // Initialize selectedAnswers list with 0 (unanswered)
        for (int i = 0; i < questions.size(); i++) {
            selectedAnswers.add(0);
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
        questionIds.clear();
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT * FROM questions WHERE exam_id = ? ORDER BY id ASC");
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                questionIds.add(rs.getInt("id"));
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

        // Restore previously selected answer if navigating back
        int prev = selectedAnswers.get(currentQuestionIndex);
        if      (prev == 1) option1.setSelected(true);
        else if (prev == 2) option2.setSelected(true);
        else if (prev == 3) option3.setSelected(true);
        else if (prev == 4) option4.setSelected(true);
        else optionsGroup.selectToggle(null);

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
            if (totalSeconds <= 0) {
                countdown.stop();
                recordCurrentAnswer();
                calculateScore();
                showResult();
            }
        }));
        countdown.setCycleCount(Animation.INDEFINITE);
        countdown.play();
    }

    private void updateTimerLabel() {
        timerLabel.setText(String.format("⏱ %02d:%02d", totalSeconds / 60, totalSeconds % 60));
        timerLabel.setStyle("-fx-text-fill: " + (totalSeconds <= 30 ? "#e74c3c" : "#6a9ae7") +
                "; -fx-font-weight: bold; -fx-font-size: 22px;");
    }

    @FXML
    private void handleNext() {
        recordCurrentAnswer();
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) loadQuestion();
        else { if (countdown != null) countdown.stop(); calculateScore(); showResult(); }
    }

    @FXML
    private void handleSubmit() {
        recordCurrentAnswer();
        if (countdown != null) countdown.stop();
        calculateScore();
        showResult();
    }

    /** Save current selection to selectedAnswers list */
    private void recordCurrentAnswer() {
        if (currentQuestionIndex >= questions.size()) return;
        RadioButton sel = (RadioButton) optionsGroup.getSelectedToggle();
        int chosen = 0;
        if      (sel == option1) chosen = 1;
        else if (sel == option2) chosen = 2;
        else if (sel == option3) chosen = 3;
        else if (sel == option4) chosen = 4;
        selectedAnswers.set(currentQuestionIndex, chosen);
    }

    /** Calculate score from selectedAnswers (+1 correct, -0.25 wrong) */
    private void calculateScore() {
        score = 0;
        for (int i = 0; i < questions.size(); i++) {
            int chosen  = selectedAnswers.get(i);
            int correct = questions.get(i).getCorrectIndex() + 1; // 1-based
            if (chosen == 0) continue; // skipped
            if (chosen == correct) score += 1;
            else score -= 0.25;
        }
        if (score < 0) score = 0;
    }

    private int saveResultToDb() {
        int resultId = -1;
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            PreparedStatement ps = connect.prepareStatement(
                    "INSERT INTO results (username, exam_id, exam_name, score, total) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, studentUsername);
            ps.setInt(2, examId);
            ps.setString(3, examName);
            ps.setDouble(4, score);
            ps.setInt(5, questions.size());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) resultId = rs.getInt(1);

        } catch (Exception e) { e.printStackTrace(); }
        return resultId;
    }

    private void saveAnswersToDb(int resultId) {
        if (resultId == -1) return;
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            PreparedStatement ps = connect.prepareStatement(
                    "INSERT INTO result_answers (result_id, question_id, question_text, " +
                            "option1, option2, option3, option4, selected_answer, correct_answer, is_correct) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?)");

            for (int i = 0; i < questions.size(); i++) {
                Question q      = questions.get(i);
                int chosen      = selectedAnswers.get(i);
                int correct     = q.getCorrectIndex() + 1; // 1-based
                boolean isCorrect = (chosen != 0 && chosen == correct);

                ps.setInt(1, resultId);
                ps.setInt(2, questionIds.get(i));
                ps.setString(3, q.getQuestion());
                ps.setString(4, q.getOptions()[0]);
                ps.setString(5, q.getOptions()[1]);
                ps.setString(6, q.getOptions()[2]);
                ps.setString(7, q.getOptions()[3]);
                ps.setInt(8, chosen);
                ps.setInt(9, correct);
                ps.setBoolean(10, isCorrect);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showResult() {
        int resultId = saveResultToDb();
        saveAnswersToDb(resultId);

        new Thread(() -> {
            EmailSender.sendResultMail(studentFatherEmail, studentUsername,
                    examName, score, questions.size());
            EmailSender.sendResultMail(studentMotherEmail, studentUsername,
                    examName, score, questions.size());
        }).start();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/buet/exam_system/Result.fxml"));
            Parent root = loader.load();
            ResultController rc = loader.getController();
            rc.setStudentInfo(studentUsername, studentEmail,
                    studentFatherEmail, studentMotherEmail, studentRole);
            rc.setResult(score, questions.size());
            rc.setResultId(resultId); // pass resultId for review

            Stage stage = (Stage) submitBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}