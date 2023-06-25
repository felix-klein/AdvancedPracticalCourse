package org.ccp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Window");

        // Create a label to display inside the window
        Label label = new Label("Hello, it can start");
        label.setTextFill(Color.YELLOW);

        // Create a layout pane and add the label to it
        StackPane root = new StackPane();
        root.getChildren().add(label);
        root.setStyle("-fx-background-color: #611e26;");

        // Create a scene with the layout pane as the root node
        Scene scene = new Scene(root, 400, 300);

        // Set the scene for the primary stage
        primaryStage.setScene(scene);

        // Display the window
        primaryStage.show();
    }
}
