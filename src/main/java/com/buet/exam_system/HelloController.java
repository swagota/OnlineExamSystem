package com.buet.exam_system;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private Hyperlink create_acc;

    @FXML
    private Label e;

    @FXML
    private Label e1;

    @FXML
    private Label exam;

    @FXML
    private Label exam1;

    @FXML
    private Hyperlink login_acc;

    @FXML
    private Button login_btn;

    @FXML
    private AnchorPane login_form;

    @FXML
    private PasswordField password;

    @FXML
    private ComboBox<String> role_box;

    @FXML
    private Button signup_btn;

    @FXML
    private AnchorPane signup_form;

    @FXML
    private TextField su_email;

    @FXML
    private PasswordField su_password;

    @FXML
    private TextField su_username;

    @FXML
    private TextField username;


    private Connection connect;
    private PreparedStatement statement;
    private ResultSet result;

    public Connection connectDb(){
        try {
            connect = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/admin",
                    "root",
                    "");
            return connect;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void login(ActionEvent event){
        connect = connectDb();
        try{
            String sql = "SELECT * FROM data WHERE username = ? and password = ?";
            statement= connect.prepareStatement(sql);
            statement.setString(1,username.getText());
            statement.setString(2,password.getText());
            result = statement.executeQuery();

            if(result.next())
            {
                int role = result.getInt("role");

                JOptionPane.showMessageDialog(null,"Successful login!","Examora Message", JOptionPane.INFORMATION_MESSAGE);

                if(role == 1) {
                    // TEACHER LOGIN
                    login_btn.getScene().getWindow().hide();
                    Parent root = FXMLLoader.load(getClass().getResource("TeacherDashboard.fxml"));

                    Scene scene = new Scene(root);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle("Teacher Dashboard");
                    stage.show();

                    // open teacher dashboard here later

                } else if(role == 2) {
                    // STUDENT LOGIN
                    login_btn.getScene().getWindow().hide();

                    // CHANGE THIS ↓ use FXMLLoader object instead
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentDashboard.fxml"));
                    Parent root = loader.load();

                    // PASS USERNAME TO DASHBOARD ↓
                    StudentDashboardController dashboardController = loader.getController();
                    dashboardController.setStudentName(username.getText());

                    Scene scene = new Scene(root);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle("Student Dashboard");
                    stage.show();
                }

            }
            else {
                JOptionPane.showMessageDialog(null,"Wrong username or password!","Examora Message", JOptionPane.ERROR_MESSAGE);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signup(ActionEvent event){
        connect = connectDb();

        try{
            String sql="INSERT INTO data VALUES(?,?,?,?)";
            statement = connect.prepareStatement(sql);
            statement.setString(1,su_username.getText());
            statement.setString(2,su_password.getText());
            statement.setString(3,su_email.getText());
            String selectedRole = role_box.getValue();
            int role;
            if(selectedRole.equals("Teacher")) {
                role = 1;
            } else {
                role = 2;
            }
            statement.setInt(4, role);
            statement.execute();

            JOptionPane.showMessageDialog(null,"Successful Create new Account!","Examora Message", JOptionPane.INFORMATION_MESSAGE);

        }catch(Exception e){
         e.printStackTrace();
        }
    }

    public void changeForm(ActionEvent event){
        if(event.getSource() == login_acc){
            signup_form.setVisible(false);
            login_form.setVisible(true);
        }
        else if(event.getSource() == create_acc){
            signup_form.setVisible(true);
            login_form.setVisible(false);
        }
    }

    public void exit(){
        System.exit(0);
    }

    public void textfield(MouseEvent event)
    {
        if(event.getSource() ==  username)
        {
            username.setStyle("-fx-background-color:#fff;"
                               + "-fx-border-width:3px;");
            password.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
        }
        else if(event.getSource() ==  password)
        {
            username.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
            password.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:3px;");
        }
    }

    public void su_textfield(MouseEvent event){

        if(event.getSource() == su_username){
            su_username.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:3px;");
            su_password.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
            su_email.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
        }
        else if(event.getSource() ==  su_password)
        {
            su_username.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
            su_password.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:3px;");
            su_email.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
        }
        else if(event.getSource() ==  su_email)
        {
            su_username.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
            su_password.setStyle("-fx-background-color:#eef3ff;"
                    + "-fx-border-width:1px;");
            su_email.setStyle("-fx-background-color:#fff;"
                    + "-fx-border-width:3px;");
        }
    }

   @Override
   public void initialize(URL url, ResourceBundle rb){
       role_box.getItems().addAll("Teacher", "Student");
       su_email.setStyle("-fx-background-color:#fff;"
               + "-fx-border-width:3px;");
       username.setStyle("-fx-background-color:#fff;"
               + "-fx-border-width:3px;");
       DropShadow original = new DropShadow(20, Color.valueOf("#6a9ae7"));
       e.setEffect(original);
       exam.setEffect(original);
       e1.setEffect(original);
       exam1.setEffect(original);

       e.setOnMouseEntered((MouseEvent event)->{
           DropShadow shadow = new DropShadow(50, Color.valueOf("#6a9ae7"));

           e.setStyle("-fx-text-fill:#fff");
           e.setEffect(shadow);
           exam.setEffect(shadow);
       });

       e.setOnMouseExited((MouseEvent event)->{
           DropShadow shadow = new DropShadow(20, Color.valueOf("#6a9ae7"));

           e.setStyle("-fx-text-fill:#6a9ae7");
           e.setEffect(shadow);
           exam.setEffect(shadow);
       });

       exam.setOnMouseEntered((MouseEvent event)->{
           DropShadow shadow = new DropShadow(50, Color.valueOf("#6a9ae7"));

           e.setStyle("-fx-text-fill:#fff");
           e.setEffect(shadow);
           exam.setEffect(shadow);
       });

       exam.setOnMouseExited((MouseEvent event)->{
           DropShadow shadow = new DropShadow(20, Color.valueOf("#6a9ae7"));

           e.setStyle("-fx-text-fill:#6a9ae7");
           e.setEffect(shadow);
           exam.setEffect(shadow);
       });

       //--------------------------------------------------------------------------------
       e1.setOnMouseEntered((MouseEvent event)->{
           DropShadow shadow = new DropShadow(50, Color.valueOf("#6a9ae7"));

           e1.setStyle("-fx-text-fill:#fff");
           e1.setEffect(shadow);
           exam1.setEffect(shadow);
       });

       e1.setOnMouseExited((MouseEvent event)->{
           DropShadow shadow = new DropShadow(20, Color.valueOf("#6a9ae7"));

           e1.setStyle("-fx-text-fill:#6a9ae7");
           e1.setEffect(shadow);
           exam1.setEffect(shadow);
       });

       exam1.setOnMouseEntered((MouseEvent event)->{
           DropShadow shadow = new DropShadow(50, Color.valueOf("#6a9ae7"));

           e1.setStyle("-fx-text-fill:#fff");
           e1.setEffect(shadow);
           exam1.setEffect(shadow);
       });

       exam1.setOnMouseExited((MouseEvent event)->{
           DropShadow shadow = new DropShadow(20, Color.valueOf("#6a9ae7"));

           e1.setStyle("-fx-text-fill:#6a9ae7");
           e1.setEffect(shadow);
           exam1.setEffect(shadow);
       });


   }
}
