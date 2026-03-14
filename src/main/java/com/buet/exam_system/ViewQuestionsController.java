package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
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
        this.teacherUsername    = username    != null ? username    : "";
        this.teacherEmail       = email       != null ? email       : "";
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
    public void initialize(URL url, ResourceBundle rb) {}

    private void loadQuestions() {
        questionsContainer.getChildren().clear();

        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            PreparedStatement ps = connect.prepareStatement(
                    "SELECT * FROM questions WHERE exam_id=? ORDER BY id ASC");
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();

            int serial = 1;
            while (rs.next()) {
                int qId         = rs.getInt("id");
                String qText    = rs.getString("question");
                String[] opts   = {
                        rs.getString("option1"), rs.getString("option2"),
                        rs.getString("option3"), rs.getString("option4")
                };
                int correct     = rs.getInt("correct_answer");
                questionsContainer.getChildren().add(
                        createQuestionCard(serial, qId, qText, opts, correct));
                serial++;
            }

            if (serial == 1) {
                Label empty = new Label("No questions yet. Add some below!");
                empty.setStyle("-fx-text-fill:#aaa;-fx-font-size:14px;-fx-font-style:italic;-fx-padding:30px;");
                questionsContainer.getChildren().add(empty);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        Button addBtn = new Button("＋  Add New Question");
        addBtn.setStyle(
                "-fx-background-color: #1a2a4a;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 10 30 10 30;" +
                        "-fx-cursor: hand;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: #6a9ae7;-fx-text-fill:white;-fx-font-size:14px;" +
                        "-fx-font-weight:bold;-fx-background-radius:10px;-fx-padding:10 30 10 30;-fx-cursor:hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: #1a2a4a;-fx-text-fill:white;-fx-font-size:14px;" +
                        "-fx-font-weight:bold;-fx-background-radius:10px;-fx-padding:10 30 10 30;-fx-cursor:hand;"));
        addBtn.setOnAction(e -> showAddQuestionDialog());

        VBox addBox = new VBox(addBtn);
        addBox.setPadding(new Insets(20, 0, 10, 0));
        addBox.setAlignment(javafx.geometry.Pos.CENTER);
        questionsContainer.getChildren().add(addBox);
    }


    private VBox createQuestionCard(int serial, int qId,
                                    String questionText, String[] options, int correctAnswer) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color:white;-fx-background-radius:12px;" +
                        "-fx-border-color:#dce8ff;-fx-border-radius:12px;-fx-border-width:1px;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);"
        );
        card.setPadding(new Insets(16, 20, 16, 20));

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label badge = new Label("Q" + serial);
        badge.setStyle("-fx-background-color:#1a2a4a;-fx-text-fill:white;-fx-font-size:11px;" +
                "-fx-font-weight:bold;-fx-background-radius:6px;-fx-padding:3px 9px;");

        Label questionLabel = new Label(questionText);
        questionLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1a2a4a;");
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(560);
        HBox.setHgrow(questionLabel, Priority.ALWAYS);
        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle(
                "-fx-background-color:#6a9ae7;-fx-text-fill:white;-fx-font-size:12px;" +
                        "-fx-background-radius:8px;-fx-padding:5 14 5 14;-fx-cursor:hand;"
        );
        editBtn.setOnAction(e -> showEditDialog(qId, questionText, options, correctAnswer));
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
                "-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:12px;" +
                        "-fx-background-radius:8px;-fx-padding:5 10 5 10;-fx-cursor:hand;"
        );
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete this question?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) deleteQuestion(qId);
            });
        });

        headerRow.getChildren().addAll(badge, questionLabel, editBtn, deleteBtn);
        card.getChildren().add(headerRow);
        Label divider = new Label();
        divider.setPrefWidth(740);
        divider.setStyle("-fx-border-color:#eef2ff;-fx-border-width:1px 0 0 0;");
        card.getChildren().add(divider);
        String[] optLabels = {"A", "B", "C", "D"};
        for (int i = 0; i < options.length; i++) {
            boolean isCorrect = (i + 1) == correctAnswer;
            HBox optBox = new HBox(10);
            optBox.setPadding(new Insets(4, 10, 4, 10));

            Label letter = new Label(optLabels[i]);
            letter.setMinWidth(24); letter.setMinHeight(24);
            letter.setStyle(isCorrect
                    ? "-fx-background-color:#27ae60;-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:50%;-fx-alignment:center;-fx-padding:3px 7px;"
                    : "-fx-background-color:#6a9ae7;-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:50%;-fx-alignment:center;-fx-padding:3px 7px;");

            Label optText = new Label(options[i]);
            optText.setWrapText(true); optText.setMaxWidth(640);
            optText.setStyle(isCorrect
                    ? "-fx-font-size:13px;-fx-text-fill:#1e8449;-fx-font-weight:bold;"
                    : "-fx-font-size:13px;-fx-text-fill:#444444;");

            optBox.setStyle(isCorrect
                    ? "-fx-background-color:#e8f8f0;-fx-background-radius:8px;-fx-border-color:#a8e6c0;-fx-border-radius:8px;-fx-border-width:1px;"
                    : "-fx-background-color:#f8faff;-fx-background-radius:8px;");

            optBox.getChildren().addAll(letter, optText);
            card.getChildren().add(optBox);
        }
        return card;
    }

    private void showEditDialog(int qId, String currentQuestion,
                                String[] currentOptions, int currentCorrect) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Question");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#f0f4ff;");
        root.setPrefWidth(550);

        Label title = new Label("✏  Edit Question");
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1a2a4a;");

        TextArea qField = new TextArea(currentQuestion);
        qField.setPromptText("Question text");
        qField.setPrefRowCount(3);
        qField.setWrapText(true);

        TextField[] optFields = new TextField[4];
        String[] optLabels = {"Option A", "Option B", "Option C", "Option D"};
        for (int i = 0; i < 4; i++) {
            optFields[i] = new TextField(currentOptions[i]);
            optFields[i].setPromptText(optLabels[i]);
        }

        ComboBox<String> correctBox = new ComboBox<>();
        correctBox.getItems().addAll("1", "2", "3", "4");
        correctBox.setValue(String.valueOf(currentCorrect));
        Label correctLabel = new Label("Correct Answer (1=A, 2=B, 3=C, 4=D):");
        correctLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#555;");

        HBox btnRow = new HBox(10);
        Button saveBtn = new Button("💾  Save Changes");
        saveBtn.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8px;-fx-padding:8 20 8 20;-fx-cursor:hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color:#bbb;-fx-text-fill:white;-fx-background-radius:8px;-fx-padding:8 20 8 20;-fx-cursor:hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            if (qField.getText().isBlank() || correctBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields!").showAndWait();
                return;
            }
            updateQuestion(qId,
                    qField.getText(),
                    optFields[0].getText(), optFields[1].getText(),
                    optFields[2].getText(), optFields[3].getText(),
                    Integer.parseInt(correctBox.getValue()));
            dialog.close();
        });

        btnRow.getChildren().addAll(saveBtn, cancelBtn);

        root.getChildren().addAll(
                title, new Label("Question:"), qField,
                new Label("Option A:"), optFields[0],
                new Label("Option B:"), optFields[1],
                new Label("Option C:"), optFields[2],
                new Label("Option D:"), optFields[3],
                correctLabel, correctBox, btnRow
        );

        dialog.setScene(new Scene(new ScrollPane(root)));
        dialog.showAndWait();
    }

    private void showAddQuestionDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Question");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#f0f4ff;");
        root.setPrefWidth(550);

        int nextNum = getNextQuestionNumber();
        Label title = new Label("＋  Add Question Q" + nextNum);
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1a2a4a;");

        TextArea qField = new TextArea();
        qField.setPromptText("Q" + nextNum + ". Enter question here...");
        qField.setPrefRowCount(3);
        qField.setWrapText(true);

        TextField opt1 = new TextField(); opt1.setPromptText("Option A");
        TextField opt2 = new TextField(); opt2.setPromptText("Option B");
        TextField opt3 = new TextField(); opt3.setPromptText("Option C");
        TextField opt4 = new TextField(); opt4.setPromptText("Option D");

        ComboBox<String> correctBox = new ComboBox<>();
        correctBox.getItems().addAll("1", "2", "3", "4");
        correctBox.setPromptText("Correct Answer");
        Label correctLabel = new Label("Correct Answer (1=A, 2=B, 3=C, 4=D):");
        correctLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#555;");

        HBox btnRow = new HBox(10);
        Button saveBtn = new Button("💾  Save Question");
        saveBtn.setStyle("-fx-background-color:#1a2a4a;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8px;-fx-padding:8 20 8 20;-fx-cursor:hand;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color:#bbb;-fx-text-fill:white;-fx-background-radius:8px;-fx-padding:8 20 8 20;-fx-cursor:hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            if (qField.getText().isBlank() || correctBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields!").showAndWait();
                return;
            }
            insertQuestion(qField.getText(),
                    opt1.getText(), opt2.getText(), opt3.getText(), opt4.getText(),
                    Integer.parseInt(correctBox.getValue()));
            dialog.close();
        });

        btnRow.getChildren().addAll(saveBtn, cancelBtn);
        root.getChildren().addAll(
                title, new Label("Question:"), qField,
                new Label("Option A:"), opt1,
                new Label("Option B:"), opt2,
                new Label("Option C:"), opt3,
                new Label("Option D:"), opt4,
                correctLabel, correctBox, btnRow
        );

        dialog.setScene(new Scene(new ScrollPane(root)));
        dialog.showAndWait();
    }

    private void updateQuestion(int qId, String question,
                                String o1, String o2, String o3, String o4, int correct) {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "UPDATE questions SET question=?, option1=?, option2=?, option3=?, option4=?, correct_answer=? WHERE id=?");
            ps.setString(1, question);
            ps.setString(2, o1); ps.setString(3, o2);
            ps.setString(4, o3); ps.setString(5, o4);
            ps.setInt(6, correct); ps.setInt(7, qId);
            ps.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION, "Question updated!").showAndWait();
            loadQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertQuestion(String question,
                                String o1, String o2, String o3, String o4, int correct) {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "INSERT INTO questions (exam_id, question, option1, option2, option3, option4, correct_answer) VALUES (?,?,?,?,?,?,?)");
            ps.setInt(1, examId);
            ps.setString(2, question);
            ps.setString(3, o1); ps.setString(4, o2);
            ps.setString(5, o3); ps.setString(6, o4);
            ps.setInt(7, correct);
            ps.executeUpdate();

            PreparedStatement upd = connect.prepareStatement(
                    "UPDATE exams SET total_marks = total_marks + 1 WHERE id=?");
            upd.setInt(1, examId);
            upd.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Question added!").showAndWait();
            loadQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteQuestion(int qId) {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "DELETE FROM questions WHERE id=?");
            ps.setInt(1, qId);
            ps.executeUpdate();

            PreparedStatement upd = connect.prepareStatement(
                    "UPDATE exams SET total_marks = total_marks - 1 WHERE id=?");
            upd.setInt(1, examId);
            upd.executeUpdate();

            loadQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNextQuestionNumber() {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            PreparedStatement ps = connect.prepareStatement(
                    "SELECT COUNT(*) FROM questions WHERE exam_id=?");
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}
