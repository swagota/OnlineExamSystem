package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AddQuestionController implements Initializable {

    @FXML
    private TextArea questionField;

    @FXML
    private TextField option1Field;

    @FXML
    private TextField option2Field;

    @FXML
    private TextField option3Field;

    @FXML
    private TextField option4Field;

    @FXML
    private ComboBox<String> correctBox;

    @FXML
    private Button backBtn;

    private Connection connect;

    @FXML
    private void saveQuestion() {

        connect = connectDb();

        try {
            if (correctBox.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Select correct answer first!");
                alert.show();
                return;
            }

            String sql = "INSERT INTO questions (question, option1, option2, option3, option4, correct_answer) VALUES (?,?,?,?,?,?)";

            PreparedStatement ps = connect.prepareStatement(sql);

            ps.setString(1, questionField.getText());
            ps.setString(2, option1Field.getText());
            ps.setString(3, option2Field.getText());
            ps.setString(4, option3Field.getText());
            ps.setString(5, option4Field.getText());
            ps.setInt(6, Integer.parseInt(correctBox.getValue()));

            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Question Saved!");
            alert.show();
            questionField.clear();
            option1Field.clear();
            option2Field.clear();
            option3Field.clear();
            option4Field.clear();
            correctBox.setValue(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/TeacherDashboard.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection connectDb() {
        try {
            connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin",
                    "root",
                    ""
            );
            return connect;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        correctBox.getItems().addAll("1", "2", "3", "4");
    }
}