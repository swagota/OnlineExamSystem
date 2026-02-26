package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SubjectPageController implements Initializable {

    @FXML
    private Button BackBtn;

    @FXML
    private ListView<String> subjectList;

    private Map<String, Integer> examMap = new HashMap<>();

    private boolean isStudentMode = false;
    private String studentUsername = "";

    public void setStudentMode(boolean isStudent, String username) {
        this.isStudentMode = isStudent;
        this.studentUsername = username;
    }

    @FXML
    private void handleBack() {
        try {
            String fxml = isStudentMode
                    ? "/com/buet/exam_system/StudentDashboard.fxml"
                    : "/com/buet/exam_system/TeacherDashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = (Stage) BackBtn.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadExams();

        subjectList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedExam = subjectList.getSelectionModel().getSelectedItem();
                if (selectedExam == null) return;

                int examId = examMap.get(selectedExam);

                if (isStudentMode) {
                    openExam(examId);
                } else {
                    openViewQuestions(examId);
                }
            }
        });
    }


    private void loadExams() {

        try {
            Connection connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin",
                    "root",
                    ""
            );

            String sql = "SELECT id, exam_name, teacher_name, total_time, total_marks FROM exams";
            PreparedStatement ps = connect.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            subjectList.getItems().clear();
            examMap.clear();

            while (rs.next()) {

                String examName = rs.getString("exam_name");
                int examId = rs.getInt("id");
                String teacher    = rs.getString("teacher_name");
                int time          = rs.getInt("total_time");
                int marks         = rs.getInt("total_marks");

                String display = examName
                        + "  |  By: " + teacher + " Sir"
                        + "  |  Time: " + time + " min"
                        + "  |  Marks: " + marks;

                subjectList.getItems().add(display);
                examMap.put(display, examId);
            }

            rs.close();
            ps.close();
            connect.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openExam(int examId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/Exam.fxml"));
            Parent root = loader.load();

            ExamController controller = loader.getController();
            controller.setExamId(examId);

            Stage stage = (Stage) subjectList.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openViewQuestions(int examId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/ViewQuestions.fxml"));
            Parent root = loader.load();

            ViewQuestionsController controller = loader.getController();
            controller.setExamId(examId);

            Stage stage = (Stage) subjectList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}