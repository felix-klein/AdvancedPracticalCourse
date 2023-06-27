package edu.gate.frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.io.File;
import java.io.IOException;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Configuration Interface for Motor Testing");
        stage.setHeight(400);
        stage.setWidth(600);
        stage.setX(450);
        stage.setY(200);

        StackPane root = new StackPane();
        Scene scene = new Scene(root);
        scene.setFill(Color.DARKGREY);

        stage.setScene(scene);
        stage.show();
    }
}
