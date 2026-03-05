package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SubjectPageController implements Initializable {

    @FXML private Button BackBtn;
    @FXML private ListView<String> subjectList;
    @FXML private Label pageTitleLabel;
    @FXML private Label infoLabel;

    private Map<String, Integer> examMap = new HashMap<>();
    private boolean isStudentMode = false;
    private String currentUsername    = "";
    private String currentEmail       = "";
    private String currentFatherEmail = "";
    private String currentMotherEmail = "";
    private int    currentRole        = 2 ;

    public void setStudentMode(boolean isStudent, String username) {
        this.isStudentMode   = isStudent;
        this.currentUsername = username != null ? username : "";
        updateLabels();
    }

    public void setUserInfo(boolean isStudent, String username, String email,
                            String fatherEmail, String motherEmail, int role) {
        this.isStudentMode      = isStudent;
        this.currentUsername    = username    != null ? username    : "";
        this.currentEmail       = email       != null ? email       : "";
        this.currentFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.currentMotherEmail = motherEmail != null ? motherEmail : "";
        this.currentRole        = role;
        updateLabels();
    }

    private void updateLabels() {
        if (pageTitleLabel != null)
            pageTitleLabel.setText(isStudentMode ? "Available Exams" : "Manage Exams");
        if (infoLabel != null)
            infoLabel.setText(isStudentMode
                    ? "Double-click on any exam to start it"
                    : "Double-click on any exam to view its questions");
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) BackBtn.getScene().getWindow();
            if (isStudentMode) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/buet/exam_system/StudentDashboard.fxml"));
                Parent root = loader.load();
                StudentDashboardController dc = loader.getController();
                dc.setStudentInfo(currentUsername, currentEmail,
                        currentFatherEmail, currentMotherEmail, currentRole);
                stage.setScene(new Scene(root));
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/buet/exam_system/teacherDashboard.fxml"));
                Parent root = loader.load();
                TeacherDashboardController tc = loader.getController();
                tc.setTeacherInfo(currentUsername, currentEmail,
                        currentFatherEmail, currentMotherEmail);
                stage.setScene(new Scene(root));
            }
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        subjectList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Parse the display string
                    String[] parts = item.split("  \\|  ");

                    VBox cell = new VBox(4);
                    cell.setPadding(new Insets(10, 15, 10, 15));

                    // Exam name row
                    Label nameLabel = new Label(parts.length > 0 ? parts[0] : item);
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a2a4a;");

                    // Details row
                    HBox details = new HBox(15);
                    for (int i = 1; i < parts.length; i++) {
                        Label detail = new Label(parts[i]);
                        detail.setStyle(
                                "-fx-font-size: 11px; -fx-text-fill: white;" +
                                        "-fx-background-color: #6a9ae7;" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-padding: 2px 8px;"
                        );
                        details.getChildren().add(detail);
                    }

                    cell.getChildren().addAll(nameLabel, details);

                    // Hover effect
                    cell.setStyle("-fx-background-color: transparent;");
                    cell.setOnMouseEntered(e ->
                            cell.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 8px;"));
                    cell.setOnMouseExited(e ->
                            cell.setStyle("-fx-background-color: transparent;"));

                    setGraphic(cell);
                    setStyle("-fx-background-color: transparent; -fx-padding: 3px 5px;");
                }
            }
        });

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

        if (isStudentMode) {
            if (pageTitleLabel != null) pageTitleLabel.setText("Available Exams");
            if (infoLabel != null) infoLabel.setText("Double-click on any exam to start it");
        } else {
            if (pageTitleLabel != null) pageTitleLabel.setText("Manage Exams");
            if (infoLabel != null) infoLabel.setText("Double-click on any exam to view its questions");
        }
    }

    private void loadExams() {
        try {
            Connection connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin", "root", "");

            String sql = "SELECT id, exam_name, teacher_name, total_time, total_marks FROM exams";
            PreparedStatement ps = connect.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            subjectList.getItems().clear();
            examMap.clear();

            while (rs.next()) {
                String examName = rs.getString("exam_name");
                int examId      = rs.getInt("id");
                String teacher  = rs.getString("teacher_name");
                int time        = rs.getInt("total_time");
                int marks       = rs.getInt("total_marks");

                String display = examName
                        + "  |  By: " + teacher + " Sir"
                        + "  |  Time: " + time + " min"
                        + "  |  Marks: " + marks;

                subjectList.getItems().add(display);
                examMap.put(display, examId);
            }

            rs.close(); ps.close(); connect.close();

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
            controller.setStudentInfo(currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail, currentRole);
            controller.setExamId(examId);
            Stage stage = (Stage) subjectList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openViewQuestions(int examId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/ViewQuestions.fxml"));
            Parent root = loader.load();
            ViewQuestionsController controller = loader.getController();
            controller.setExamId(examId);
            controller.setTeacherInfo(currentUsername, currentEmail,
                    currentFatherEmail, currentMotherEmail);
            Stage stage = (Stage) subjectList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}