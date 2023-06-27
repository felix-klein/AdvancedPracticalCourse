package edu.gate.frontend;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        Group root1 = new Group();
        Scene scene = new Scene(root1);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
