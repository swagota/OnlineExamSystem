package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ViewQuestionsController implements Initializable {

    @FXML
    private Button backBtn;

    @FXML
    private void handleBack() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/SubjectPage.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private ListView<String> questionList;

    private String subject;

    public void setSubject(String subject) {
        this.subject = subject;
        loadQuestions();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // এখানে কিছু করো না
    }

    private void loadQuestions() {

        try {
            Connection connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin",
                    "root",
                    ""
            );

            String sql = "SELECT question FROM questions WHERE subject = ?";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setString(1, subject);

            ResultSet rs = ps.executeQuery();

            questionList.getItems().clear();

            int serial = 1;

            while (rs.next()) {
                questionList.getItems().add(serial + ". " + rs.getString("question"));
                serial++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}