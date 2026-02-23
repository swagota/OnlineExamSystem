package com.buet.exam_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    private double x=0;
    private double y=0;
    @Override
    public void start(Stage stage) throws IOException {
        Parent root= FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Scene scene = new Scene(root);

        root.setOnMousePressed((MouseEvent event)->{
            x = event.getSceneX();
            y = event.getSceneY();
        });

        root.setOnMouseDragged((MouseEvent event)->{
            stage.setX(event.getScreenX()-x);
            stage.setY(event.getScreenY()-y);
        });

        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setTitle("EXAMORA");
        stage.setScene(scene);
        stage.show();
    }
}
