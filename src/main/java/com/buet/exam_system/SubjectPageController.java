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
import java.util.ResourceBundle;

public class SubjectPageController implements Initializable {

    @FXML
    private Button BackBtn;

    @FXML
    private void handleBack() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/TeacherDashboard.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) BackBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private ListView<String> subjectList;



    private static String mode;   // ADD or VIEW

    public static void setMode(String m) {
        mode = m;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        subjectList.getItems().addAll(
                "Mathematics",
                "Physics",
                "CSE"
        );

        subjectList.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                String selectedSubject =
                        subjectList.getSelectionModel().getSelectedItem();

                try {

                    if (mode.equals("ADD")) {

                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/buet/exam_system/AddQuestion.fxml")
                        );

                        Parent root = loader.load();

                        AddQuestionController controller =
                                loader.getController();

                        controller.setSubject(selectedSubject);

                        Stage stage =
                                (Stage) subjectList.getScene().getWindow();

                        stage.setScene(new Scene(root));
                        stage.show();
                    }

                    else {   // VIEW MODE

                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/buet/exam_system/ViewQuestions.fxml")
                        );

                        Parent root = loader.load();

                        ViewQuestionsController controller =
                                loader.getController();

                        controller.setSubject(selectedSubject);

                        Stage stage =
                                (Stage) subjectList.getScene().getWindow();

                        stage.setScene(new Scene(root));
                        stage.show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}