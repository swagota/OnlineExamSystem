package com.buet.exam_system;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AddQuestionController implements Initializable {

    @FXML private TextArea questionField;
    @FXML private TextField option1Field;
    @FXML private TextField option2Field;
    @FXML private TextField option3Field;
    @FXML private TextField option4Field;
    @FXML private ComboBox<String> correctBox;
    @FXML private Button backBtn;
    @FXML private Label examInfoLabel;

    private int examId;
    private String teacherName;
    private String teacherEmail       = "";
    private String teacherFatherEmail = "";
    private String teacherMotherEmail = "";

    public void setTeacherInfo(String name, String email,
                               String fatherEmail, String motherEmail) {
        this.teacherName        = name        != null ? name        : "";
        this.teacherEmail       = email       != null ? email       : "";
        this.teacherFatherEmail = fatherEmail != null ? fatherEmail : "";
        this.teacherMotherEmail = motherEmail != null ? motherEmail : "";
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        correctBox.getItems().addAll("1", "2", "3", "4");
    }

    public void setExamId(int examId) {
        this.examId = examId;
        updatePromptText();
    }

    public void setTeacherName(String name) {
        this.teacherName = name;
    }

    private void updatePromptText() {
        int nextNumber = getNextQuestionNumber();
        questionField.setPromptText("Q" + nextNumber + ". Enter question here...");
    }

    private int getNextQuestionNumber() {
        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {
            String sql = "SELECT COUNT(*) FROM questions WHERE exam_id = ?";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/buet/exam_system/teacherDashboard.fxml"));
            Scene scene = new Scene(loader.load());

            TeacherDashboardController controller = loader.getController();
            controller.setTeacherInfo(teacherName,teacherEmail,
                    teacherFatherEmail, teacherMotherEmail);

            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        if (examId == 0) {
            System.out.println("Exam ID not set!");
            return;
        }

        if (questionField.getText().isBlank() || correctBox.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Incomplete");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all fields and select the correct option.");
            alert.showAndWait();
            return;
        }

        try (Connection connect = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/admin", "root", "")) {

            String sql = "INSERT INTO questions (exam_id, question, option1, option2, option3, option4, correct_answer) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement ps = connect.prepareStatement(sql);
            ps.setInt(1, examId);
            ps.setString(2, questionField.getText());
            ps.setString(3, option1Field.getText());
            ps.setString(4, option2Field.getText());
            ps.setString(5, option3Field.getText());
            ps.setString(6, option4Field.getText());
            ps.setInt(7, Integer.parseInt(correctBox.getValue()));
            ps.executeUpdate();

            String updateSql = "UPDATE exams SET total_marks = total_marks + 1 WHERE id=?";
            PreparedStatement updatePs = connect.prepareStatement(updateSql);
            updatePs.setInt(1, examId);
            updatePs.executeUpdate();

            questionField.clear();
            option1Field.clear();
            option2Field.clear();
            option3Field.clear();
            option4Field.clear();
            correctBox.setValue(null);
            updatePromptText();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Question Saved Successfully!");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}