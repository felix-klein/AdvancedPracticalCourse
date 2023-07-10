package edu.gate.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * The AdminController is the controller class for all events from the AdminScene.fxml.
 */
public class AdminController extends ComboController {
    @FXML
    CheckBox check1, check2, check3, check4, check5, check6, check7;

    /**
     * This method does the switch to the User-Scene page.
     *
     * @param action is the Event to switch the scene.
     * @throws IOException to handle event errors.
     */
    @FXML
    private void switchToUserScene(ActionEvent action) throws IOException {
        new SceneControl(true, action, null);
    }

    /**
     * This method initializes the program with the blueprint engine, if all checkboxes are ticked.
     *
     * @param mouseEvent is the Event to start the initialization.
     */
    @FXML
    private void setup(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - Start Initialization: " + mouseEvent.getPickResult());
        /* Control if all checkboxes are ticket. If no checkbox is not ticked.*/
        if (!check1.isSelected()
                || !check2.isSelected()
                || !check3.isSelected()
                || !check4.isSelected()
                || !check5.isSelected()
                || !check6.isSelected()
                || !check7.isSelected()) {
            String headline = "missing checkboxes";
            String text = "Unfortunately, you cannot start the process yet because you have not completed or checked" +
                    " off all the tasks from the task list. Please make sure that all tasks are completed" +
                    " consistently and conscientiously before you start the process.";
            infoWindow(headline, text);
            return; /* If there is one missing, we do not initialize and return */
        }

        try {
            /* Pass the command to initialize the Blueprint. */
            File file = new File(Objects.requireNonNull(getClass()
                    .getResource("/adminFiles/setup.bpmn")).toURI());
            MainApplication.getTerminal().initializeBlueprint(file);
            infoWindow("Motor Initialization Done", "The blueprint motor is now fully initialized " +
                        "and ready to use for further testing!");
        } catch (URISyntaxException e) {
            System.out.println("There is an error with the admin file or its path");
            e.getStackTrace();
        }
    }
}
