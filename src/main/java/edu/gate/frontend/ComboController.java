package edu.gate.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The ComboController is a mather class for the AdminController and UserController to inherit methods.
 */
public class ComboController {

    /**
     * This method does show us the information window from the Accuracy area.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonAccuracy(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info accuracy: " + mouseEvent.getPickResult());
        String headline = "Accuracy Information";
        String text = "Accuracy describes how accurate the sensor data for the compliance check should be." +
                " We recommend the level basic to ensure both " +
                "runtime and correctness of the compliance checking results.";
        infoWindow(headline, text);
    }

    /**
     * This method does show us the information window from the Loop area.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonLoops(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info loops: " + mouseEvent.getPickResult());
        String headline = "Loop runs";
        String text = "If you have consciously or unconsciously integrated one or more loops in your test " +
                "process, you must specify the number of loop runs intended.";
        infoWindow(headline, text);
    }

    /**
     * This method does show us the information window from the Loop area.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonSelection(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info selection: " + mouseEvent.getPickResult());
        String headline = "Process Selection";
        String text = "You can select only one of the processes to run. To edit or view the processes, please go" +
                " to the 'Customization Area'.";
        infoWindow(headline, text);
    }

    /**
     * This method does show us the information window from the customizable processes.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonCustomize(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info customization: " + mouseEvent.getPickResult());
        String headline = "Process Customization";
        String text = "These 3 process environments can be customized and modified by you. Please follow the" +
                " instructions on the right and save your changes with the save button. Do not change the location!";
        infoWindow(headline, text);
    }

    /**
     * This method does show us the information window from the not customizable processes.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonView(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info view: " + mouseEvent.getPickResult());
        String headline = "Predefined Models";
        String text = "We ask that you view, but not change, these predefined processes. If you do change them, " +
                "please follow the given notations.";
        infoWindow(headline, text);
    }

    /**
     * This method does show us the information window from the setup area with the checkboxes.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonSetup(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info setup: " + mouseEvent.getPickResult());
        String headline = "Test-Bench Setup";
        String text = "All items in this list are essential for the smooth and correct initialization of the" +
                " Blueprint engine. If you have conscientiously worked through all the points and start the program," +
                " the data acquisition for the engine can succeed in the best possible way to make the further user" +
                " tests as accurate as possible.\n" +
                "You must also have completed all points to be able to start the initialization.";
        infoWindow(headline, text);
    }

    /**
     * This method is a helper to reduce code duplicates for the screening of the information windows.
     *
     * @param headline is the Text for the window title.
     * @param text     is the text for the information itself.
     */
    protected void infoWindow(String headline, String text) {
        Alert panel = new Alert(Alert.AlertType.INFORMATION); /* create an alert panel */
        panel.setTitle(headline); /* panel title */
        panel.setHeaderText(null); /* no header */
        panel.setX(881.0); /* window position */
        panel.setY(425.0); /* window position */
        panel.setContentText(text);
        panel.showAndWait();
    }

    /**
     * This Method opens the Browser with the tap of the correct modeller instance or a picture of the process model.
     * TODO: Open Modeller needs real options, until now there are just dummies.
     * @param type is the selected model.
     */
    protected void openModeller(String type) {
        String path = switch (type) {
            case "alpha" -> "https://cpee.org/flow/motor.html?monitor=https://cpee.org/flow/engine/21677/";
            case "beta" -> "Still blank and should be changed with option 2";
            case "gamma" -> "Still blank and should be changed with option 3";
            case "longR" -> "Predefined and should just display an image";
            default -> "Predefined and should just display an image for the longR";
        };

        try {
            // Create a URI object from the URL string
            URI uri = new URI(path);

            // Check if the Desktop class is supported (e.g., on Windows, macOS, and some Linux distributions)
            if (Desktop.isDesktopSupported()) {
                // Open the default web browser with the given URI
                Desktop.getDesktop().browse(uri);
            } else {
                // Desktop is not supported (e.g., on some Linux distributions)
                infoWindow("Browser Error", "You need to change your default web browser or inform " +
                        "your Admin!");
                System.out.println("You need to change your default web browser or inform your Admin!");
            }
        } catch (URISyntaxException | IOException e) {
            infoWindow("Modeller Error", "The link to the modeller is missing or wrong." +
                    " Please inform your Admin!");
            throw new RuntimeException(e);
        }
    }
}
