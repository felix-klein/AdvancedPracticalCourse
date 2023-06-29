package edu.gate.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class ComboController {

    /**
     * This method does show us the information window from the Accuracy area.
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonAccuracy(javafx.scene.input.MouseEvent mouseEvent) {
        Alert panel = new Alert(Alert.AlertType.INFORMATION); /* create an alert panel */
        panel.setTitle("Accuracy Information"); /* panel title */
        panel.setHeaderText(null); /* no header */
        panel.setX(881.0); /* window position */
        panel.setY(425.0); /* window position */
        panel.setContentText("This is the information you want to display."); //TODO: Window context
        panel.showAndWait();
    }
}
