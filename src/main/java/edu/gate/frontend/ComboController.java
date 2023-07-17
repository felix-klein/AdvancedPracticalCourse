package edu.gate.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

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
     * This method is needed to open the Camunda Modeler with the corresponding file, and it's process.
     *
     * @param type is the filename to open.
     * @throws URISyntaxException is an exception handler for the file transformation.
     * @throws IOException        is an exception handler for the OS selection.
     */
    protected void openCamundaFile(String type) throws URISyntaxException, IOException {
        File file = getRequestedFile(type); /* to get the requested and needed file */

        /* Control if the file is even existing with its path, and return an info window for the user. */
        if (!file.exists()) {
            infoWindow("File Error", "The file for the process is missing of saved wrongly." +
                    " Please inform your Admin!");
        } else {
            openInCorrectOS(file); /* open the file, but with the right comment for the OS */
        }
    }

    /**
     * This method is a helper to reduce code duplication and to be open for the Terminal call via UserController.
     *
     * @param type is the id name of the requested file.
     * @return the file of type.
     * @throws URISyntaxException is an exception handler for the file transformation.
     */
    protected File getRequestedFile(String type) throws URISyntaxException {
        /* Switch to indicate the needed and correct file path. */
        String path = switch (type) {
            case "alpha" -> "/camundaFiles/alpha.bpmn";
            case "beta" -> "/camundaFiles/beta.bpmn";
            case "gamma" -> "/camundaFiles/gamma.bpmn";
            case "longR" -> "/camundaFiles/LongRun.bpmn";
            default -> "/camundaFiles/ShortRun.bpmn";
        };
        return new File(Objects.requireNonNull(getClass().getResource(path)).toURI()); /* get file */
    }

    /**
     * This method is a helper method for the openCamundaFile method, to operate the correct command for the operating
     * system of the user.
     *
     * @param file is the file to open.
     * @throws IOException is an exception handler for the OS selection.
     */
    private void openInCorrectOS(File file) throws IOException {
        /* Request the name of the OS from the users' device. */
        String operatingSystem = System.getProperty("os.name").toLowerCase();

        /* Compare the String with the OS indication with the indicators for those systems and run their corresponding
           operation to open the file.
         */
        if (operatingSystem.contains("mac")) { /* Mac*/
            new ProcessBuilder("open", file.getAbsolutePath()).start();
        } else if (operatingSystem.contains("win")) { /* Windows */
            new ProcessBuilder("cmd.exe", "/c", "start", file.getAbsolutePath()).start();
        } else if (operatingSystem.contains("nux") || operatingSystem.contains("nix")) { /* Linux & Unix */
            new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
        } else {
            infoWindow("File Error by OS determination", "It is not possible to open the document" +
                    " because it is not possible to find out on which operating system this application is running." +
                    " Please contact the developer, the admin or switch to a Windows/Mac/Linux/Unix operating system.");
        }
    }
}
