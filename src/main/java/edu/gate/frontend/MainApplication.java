package edu.gate.frontend;

import edu.ground.analysis.ComplianceResults;
import edu.terminal.Terminal;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * MainApplication extended with the Application class for JavaFX. It is the entrance class for the frontend/user to
 * the system and the start of the whole program. It is important to start via Maven run and not Main run, as described
 * in the README.txt.
 */
public class MainApplication extends Application {
    private static Terminal terminal;
    private ComplianceResults complianceResults;

    /**
     * The main method is for JavaFX purposes initialized with a command.
     *
     * @param args is the Java execution argument.
     */
    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "javafx");
        terminal = new Terminal();
        launch(args);
    }

    /**
     * Comments after completion!
     *
     * @param primaryStage is the basic and first stage object for the JavaFX structure.
     */
    @Override
    public void start(Stage primaryStage) {
        /* For error handling, the implementation of a try-catch structure is helpful */
        try {
            /* Initializing the scene creation class to create a JavaFX application scene */
            new SceneControl((short) 0, null, primaryStage);

            /* Creation of a secondary Stage for the by-Window (Process and Results) */
            setComplianceResults();
            // Stage secondaryStage = new Stage();
            // new SceneControl((short) 2, null, secondaryStage);
        } catch (Exception fx) { /* Throw a general text and additional information */
            System.out.println("There is an error in the JavaFX application or in one of the related applications!");
            fx.getCause();
        }
    }

    /**
     * This getter allows all classes of this package to use the current terminal. One of the most important functions
     * for further backend analyses and processing.
     *
     * @return the actual terminal object.
     */
    public static Terminal getTerminal() {
        return terminal;
    }

    /**
     * Setter for the startUserProcess method in Terminal to initialize the results for the Scene Builder of Results.
     */
    public void setComplianceResults() {
        this.complianceResults = terminal.getComplianceResults();
        /* Right after initialization, the result window can be shown. */
        try {
            /* Creation of a secondary Stage for the by-Window (Process and Results) */
            Stage secondaryStage = new Stage();
            new SceneControl((short) 2, null, secondaryStage);
        } catch (Exception fx) { /* Throw a general text and additional information */
            System.out.println("There is an error in the JavaFX application or in one of the related applications!");
            fx.getCause();
        }
    }

    /**
     * Getter of the results for the ResultScene.
     *
     * @return a compliance result object.
     */
    public ComplianceResults getComplianceResults() {
        return complianceResults;
    }
}
