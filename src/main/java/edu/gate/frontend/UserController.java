package edu.gate.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The UserController is the controller class for all events from the UserScene.fxml.
 */
public class UserController extends ComboController implements Initializable {
    @FXML
    private RadioButton alpha, beta, gamma, shortR, longR;
    @FXML
    private Spinner<Integer> loopRun;
    @FXML
    private ChoiceBox<String> accuracy;

    /**
     * The method process is activated, if the Process button is clicked. It is the main method of the whole
     * application. If the user did no changes, the default values will be used to process the application.
     *
     * @param mouseEvent is the mouseclick to run the process.
     * @throws URISyntaxException is an exception handler for the file transformation.
     */
    @FXML
    private void process(javafx.scene.input.MouseEvent mouseEvent) throws URISyntaxException {
        String type;
        /* Determine which of the processes the user selected. */
        if (alpha.isSelected()) { /* alpha: first customized process */
            type = "alpha"; // just testing
        } else if (beta.isSelected()) { /* beta: second customized process */
            type = "beta";
        } else if (gamma.isSelected()) { /* gamma: third customized process */
            type = "gamma";
        } else if (longR.isSelected()) { /* longR: the predefined long run process */
            type = "longR";
        } else { /* shortR: the predefined short run process, also default value*/
            type = "shortR";
        }
        /* Activates the process with settings from the Control Center. */
        MainApplication.getTerminal().startUserProcess(getRequestedFile(type), accuracy.getValue(), loopRun.getValue());
    }

    /**
     * This method does the switch to the Admin-Scene page.
     *
     * @param action is the Event to switch the scene.
     * @throws IOException to handle event errors.
     */
    @FXML
    private void switchToAdminScene(ActionEvent action) throws IOException {
        new SceneControl(false, action, null);
    }

    /**
     * This method is a default method via the Initializable implementation. It's main purpose is to initialize
     * the box areas loopRun (Spinner) and accuracy (ChoiceBox).
     *
     * @param url            is a JavaFX argument.
     * @param resourceBundle is a JavaFX argument.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* Initializing the Spinner for the loop runs */
        SpinnerValueFactory<Integer> spinnerLoops =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20); /* interval */
        spinnerLoops.setValue(1); /* default */
        loopRun.setValueFactory(spinnerLoops);

        /* Initializing the ChoiceBox for the accuracy levels */
        String[] level = new String[]{"extreme", "intense", "high", "basic", "1-for-1"}; /* choice options */
        accuracy.getItems().addAll(level);
        accuracy.setValue("basic"); /* default */
    }

    /**
     * This method opens the Camunda Modeler with the ALPHA file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     * @throws URISyntaxException is an exception handler for the file transformation.
     * @throws IOException        is an exception handler for the OS selection.
     */
    @FXML
    private void alphaView(javafx.scene.input.MouseEvent mouseEvent) throws URISyntaxException, IOException {
        openCamundaFile("alpha");
    }

    /**
     * This method opens the Camunda Modeler with the BETA file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     * @throws URISyntaxException is an exception handler for the file transformation.
     * @throws IOException        is an exception handler for the OS selection.
     */
    @FXML
    private void betaView(javafx.scene.input.MouseEvent mouseEvent) throws URISyntaxException, IOException {
        openCamundaFile("beta");
    }

    /**
     * This method opens the Camunda Modeler with the GAMMA file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     * @throws URISyntaxException is an exception handler for the file transformation.
     * @throws IOException        is an exception handler for the OS selection.
     */
    @FXML
    private void gammaView(javafx.scene.input.MouseEvent mouseEvent) throws URISyntaxException, IOException {
        openCamundaFile("gamma");
    }

    /**
     * This method opens the Camunda Modeler with the LONG-RUN file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     * @throws URISyntaxException is an exception handler for the file transformation.
     * @throws IOException        is an exception handler for the OS selection.
     */
    @FXML
    private void longRView(javafx.scene.input.MouseEvent mouseEvent) throws URISyntaxException, IOException {
        openCamundaFile("longR");
    }

    /**
     * This method opens the Camunda Modeler with the SHORT-RUN file via mouse click.
     *
     * @param mouseEvent is the event to open the process modeler.
     * @throws URISyntaxException is an exception handler for the file transformation.
     * @throws IOException        is an exception handler for the OS selection.
     */
    @FXML
    private void shortRView(javafx.scene.input.MouseEvent mouseEvent) throws URISyntaxException, IOException {
        /* The passed parameter is unimportant, because the short-run represents the default operation. */
        openCamundaFile("shortR"); /* could be null with no influence */
    }
}
