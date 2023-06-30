package edu.gate.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * The AdminController is the controller class for all events from the AdminScene.fxml.
 */
public class AdminController extends ComboController {

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
}
