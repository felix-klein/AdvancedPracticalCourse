package edu.gate.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * The UserController is the controller class for all events from the UserScene.fxml.
 */
public class UserController extends ComboController{

    /**
     * This method does the switch to the Admin-Scene page.
     * @param action is the Event to switch the scene.
     * @throws IOException to handle event errors.
     */
    @FXML
    private void switchToAdminScene(ActionEvent action) throws IOException {
        new SceneControl(false, action, null);
    }
}
