package edu.gate.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * MainApplication extended with the Application class for JavaFX. It is the entrance class for the frontend/user to
 * the system and the start of the whole program. It is important to start via Maven run and not Main run, as described
 * in the README.txt.
 */
public class MainApplication extends Application {
    /**
     * The main method is for JavaFX purposes initialized with a command.
     * @param args Java execution arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Comments after completion!
     * @param primaryStage is the basic and first stage object for the JavaFX structure
     */
    @Override
    public void start(Stage primaryStage) {
        /* For error handling, the implementation of a try-catch structure is helpful */
        try {

            /* JavaFX root1 element for the first group of Scene-Graph elements (element grouping - low) */
            Parent root1 = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/layout/SceneCreator.fxml"))); /* load the fxml file */

            /* JavaFX scene including the first group for the scene creation (visualisation content - middle) */
            Scene scene = new Scene(root1);

            /* JavaFX stage as the main window for the whole application (application window - high) */
            primaryStage.setScene(scene);
            primaryStage.setX(781.0); /* window position */
            primaryStage.setY(25.0); /* window position */
            primaryStage.setTitle("Motor Quality Control Application"); /* set title */
            primaryStage.show();

        } catch (Exception fx) { /* Throw a general text and additional information */
            System.out.println("There is an error in the JavaFX application or in one of the related applications!");
            fx.printStackTrace();
        }
    }
}