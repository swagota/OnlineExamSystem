package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExamController implements Initializable {

    @FXML private Label questionLabel;
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

    private Connection connect;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        optionsGroup = new ToggleGroup();
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);

        loadQuestionsFromDatabase();

        if (!questions.isEmpty()) {
            loadQuestion();
        }
    }

    private void loadQuestionsFromDatabase() {

        try {
            connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin",
                    "root",
                    ""
            );

            String sql = "SELECT * FROM questions";
            PreparedStatement ps = connect.prepareStatement(sql);
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

        questionLabel.setText(q.getQuestion());

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
            showResult();
        }
    }

    @FXML
    private void handleSubmit() {
        checkAnswer();
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

        questionLabel.setText("Exam Finished! Your Score: "
                + score + " / " + questions.size());

        option1.setDisable(true);
        option2.setDisable(true);
        option3.setDisable(true);
        option4.setDisable(true);

        nextBtn.setDisable(true);
        submitBtn.setDisable(true);
    }
}