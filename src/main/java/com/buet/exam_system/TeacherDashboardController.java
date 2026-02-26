package com.buet.exam_system;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

import javafx.scene.Parent;
import javafx.scene.control.Button;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class TeacherDashboardController implements Initializable {
//    @FXML
//    private void saveQuestion() {
//        System.out.println("Save clicked");
//    }

    @FXML
    private Button addQuestionBtn;

    @FXML
    private Button availableExamBtn;

    @FXML
    private Button leaderboardBtn;
    private String teacherName;
    public void setTeacherName(String name) {
        this.teacherName = name;
    }
    @FXML
    private void handleAddQuestion() {

        //System.out.println("Button clicked");

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/CreateExam.fxml")
            );

            //System.out.println("FXML loading...");

            Scene scene = new Scene(loader.load());
            CreateExamController controller = loader.getController();
            controller.setTeacherName(teacherName);
            Stage stage = (Stage) addQuestionBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLeaderboard() {
        System.out.println("Leaderboard clicked");
    }

    @FXML
    private void handleAvailableExams() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/SubjectPage.fxml"));

            Parent root = loader.load();

            SubjectPageController controller = loader.getController();
            controller.setStudentMode(false, "");

            Stage stage = (Stage) availableExamBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}