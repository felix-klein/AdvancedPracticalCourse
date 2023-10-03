package edu.gate.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The UserController is the controller class for all events from the UserScene.fxml.
 */
public class UserController extends ComboController implements Initializable {
    @FXML
    private RadioButton alpha, beta, gamma, longR; /*  shortR: isn't needed, because it is the default value */
    @FXML
    private ChoiceBox<String> accuracy;
    @FXML
    private Slider deviation, acceptance;
    private int deviationPercentage;
    private int acceptancePercentage;
    @FXML
    private Label deviationLabel, acceptanceLabel;

    /**
     * The method process is activated, if the Process button ("run test") is clicked. It is the main method of the whole
     * application. If the user did no changes, the default values will be used to process the application.
     *
     * @param mouseEvent is the mouseclick to run the process.
     */
    @FXML
    private void process(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - RUN TEST: " + mouseEvent.getPickResult());
        /* Activates the process with settings from the Control Center. */
        boolean processRunning = MainApplication.getTerminal().startUserProcess(getType(), accuracy.getValue());

        /* If the admin has not initialized the blueprint engine yet, the user can not start the process. */
        if (!processRunning) {
            infoWindow("No Process Start Possible", "Your admin has not yet initialized the Blueprint" +
                    " engine into the system. The comparison data to compare your process with ideal values is " +
                    "missing. Please inform your admin about the missing initialization and wait until it is active.");
        }
    }

    /**
     * Helper method to get the selected type (the running process of choice).
     *
     * @return the type as a String.
     */
    private String getType() {
        String type;
        /* Determine which of the processes the user selected. */
        if (alpha.isSelected()) { /* alpha: first customized process */
            type = "alpha";
        } else if (beta.isSelected()) { /* beta: second customized process */
            type = "beta";
        } else if (gamma.isSelected()) { /* gamma: third customized process */
            type = "gamma";
        } else if (longR.isSelected()) { /* longR: the predefined long run process */
            type = "longR";
        } else { /* shortR: the predefined short run process, also default value */
            type = "shortR";
        }
        return type;
    }

    /**
     * This method does the switch to the Admin-Scene page.
     *
     * @param action is the Event to switch the scene.
     * @throws IOException to handle event errors.
     */
    @FXML
    private void switchToAdminScene(ActionEvent action) throws IOException {
        new SceneControl((short) 1, action, null);
    }

    /**
     * This method is a default method via the Initializable implementation. It's main purpose is to initialize
     * the box accuracy (ChoiceBox).
     *
     * @param url            is a JavaFX argument.
     * @param resourceBundle is a JavaFX argument.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* Initializing the ChoiceBox for the accuracy levels. */
        String[] level = new String[]{"extreme", "intense", "high", "basic", "1-for-1"}; /* choice options */
        accuracy.getItems().addAll(level);
        accuracy.setValue("basic"); /* default */

        /* Initializing the Slider for the deviation. */
        deviationPercentage = (int) deviation.getValue();
        deviationLabel.setText(deviationPercentage + "%");
        deviation.valueProperty().addListener((observableValue, number, t1) -> {
            deviationPercentage = (int) deviation.getValue();
            deviationLabel.setText(deviationPercentage + "%");
        });

        /* Initializing the Slider for the acceptance. */
        acceptancePercentage = (int) acceptance.getValue();
        acceptanceLabel.setText(acceptancePercentage + "%");
        acceptance.valueProperty().addListener((observableValue, number, t1) -> {
            acceptancePercentage = (int) deviation.getValue();
            acceptanceLabel.setText(acceptancePercentage + "%");
        });
    }

    /**
     * This method opens the Camunda Modeler with the ALPHA file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     */
    @FXML
    private void alphaView(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - Alpha: " + mouseEvent.getPickResult());
        openModeller("alpha");
    }

    /**
     * This method opens the Camunda Modeler with the BETA file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     */
    @FXML
    private void betaView(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - Beta: " + mouseEvent.getPickResult());
        openModeller("beta");
    }

    /**
     * This method opens the Camunda Modeler with the GAMMA file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     */
    @FXML
    private void gammaView(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - Gamma: " + mouseEvent.getPickResult());
        openModeller("gamma");
    }

    /**
     * This method opens the Camunda Modeler with the LONG-RUN file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     */
    @FXML
    private void longRView(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - Long-Run: " + mouseEvent.getPickResult());
        openModeller("longR");
    }

    /**
     * This method opens the Camunda Modeler with the SHORT-RUN file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     */
    @FXML
    private void shortRView(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - Short-Run: " + mouseEvent.getPickResult());
        /* The passed parameter is unimportant, because the short-run represents the default operation. */
        openModeller("shortR"); /* could be null with no influence */
    }
}
