package edu.gate.frontend;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * MainApplication extended with the Application class for JavaFX. It is the entrance class for the frontend/user to
 * the system and the start of the whole program. It is important to start via Maven run and not Main run, as described
 * in the README.txt.
 */
public class MainApplication extends Application {
    /**
     * The main method is for JavaFX purposes initialized with a command.
     * @param args is the Java execution argument.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Comments after completion!
     * @param primaryStage is the basic and first stage object for the JavaFX structure.
     */
    @Override
    public void start(Stage primaryStage) {
        /* For error handling, the implementation of a try-catch structure is helpful */
        try {
            /* Initializing the scene creation class to create a JavaFX application scene */
            new SceneControl(true, null, primaryStage);

        } catch (Exception fx) { /* Throw a general text and additional information */
            System.out.println("There is an error in the JavaFX application or in one of the related applications!");
            fx.printStackTrace();
        }
    }
}