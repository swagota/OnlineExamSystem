package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class ExamController {

    @FXML private Label questionLabel;
    @FXML private RadioButton option1;
    @FXML private RadioButton option2;
    @FXML private RadioButton option3;
    @FXML private RadioButton option4;
    @FXML private Button nextBtn;
    @FXML private Button submitBtn;

    private ToggleGroup optionsGroup;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;

    @FXML
    public void initialize() {

        optionsGroup = new ToggleGroup();
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);


        questions = new ArrayList<>();

        questions.add(new Question(
                "What is Java?",
                new String[]{"Programming Language", "Animal", "Car", "Fruit"},
                0
        ));

        questions.add(new Question(
                "2 + 2 = ?",
                new String[]{"3", "4", "5", "6"},
                1
        ));

        questions.add(new Question(
                "Which one is OOP concept?",
                new String[]{"Encapsulation", "Photosynthesis", "Gravity", "Oxygen"},
                0
        ));

        loadQuestion();
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

        if (selectedIndex == questions.get(currentQuestionIndex).getCorrectIndex()) {
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
