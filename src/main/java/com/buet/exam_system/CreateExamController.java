package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class CreateExamController {

    @FXML private TextField examNameField;
    @FXML private TextField totalTimeField;
    @FXML private Label teacherNameLabel;

    private String teacherName = "Teacher"; // later login থেকে আনবে

    @FXML
    public void initialize() {
        teacherNameLabel.setText("By — " + teacherName + " Sir");
    }

    public void setTeacherName(String name) {
        this.teacherName = name;
        if (teacherNameLabel != null) {
            teacherNameLabel.setText("By — " + teacherName + " Sir");
        }
    }

    @FXML
    private Button backBtn;

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/TeacherDashboard.fxml"));

            Scene scene = new Scene(loader.load());

            TeacherDashboardController controller = loader.getController();
            controller.setTeacherName(teacherName);

            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreate() {

        String examName = examNameField.getText().trim();
        String timeText = totalTimeField.getText().trim();

        if (examName.isBlank() || timeText.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete");
            alert.setHeaderText(null);
            alert.setContentText("Please enter exam name and time.");
            alert.showAndWait();
            return;
        }

        try {
            Connection connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin",
                    "root",
                    ""
            );

            String sql = "INSERT INTO exams (exam_name, total_time,total_marks, teacher_name) VALUES (?,?,?,?)";
            PreparedStatement ps = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, examName);
            ps.setInt(2, Integer.parseInt(timeText));
            ps.setInt(3,0);
            ps.setString(4,teacherName);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
//            int newExamId=0;
            if (rs.next()) {
                int examId = rs.getInt(1);

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/buet/exam_system/AddQuestion.fxml")
                );

                Parent root = loader.load();

                AddQuestionController controller = loader.getController();
                controller.setExamId(examId);

                Stage stage = (Stage) examNameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}