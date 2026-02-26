package com.buet.exam_system;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

import javafx.scene.control.Button;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class TeacherDashboardController implements Initializable {

    @FXML
    private Button addQuestionBtn;

    @FXML
    private void handleAddQuestion() {

        //System.out.println("Button clicked");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/AddQuestion.fxml")
            );

            //System.out.println("FXML loading...");

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) addQuestionBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}