package edu.gate.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * The ComboController is a mather class for the AdminController and UserController to inherit methods.
 */
public class ComboController {
    private static final String webBody = """
            <testset xmlns="http://cpee.org/ns/properties/2.0">
              <executionhandler>ruby</executionhandler>
              <dataelements/>
              <endpoints>
                <motor_start>motor_start</motor_start>
                <motor_stop>motor_stop</motor_stop>
                <motor_gear>motor_gear</motor_gear>
                <motor_hull_sensor>motor_hull_sensor</motor_hull_sensor>
              </endpoints>
              <attributes>
                <info>Motor</info>
                <modeltype>motor</modeltype>
                <theme>motor</theme>
              </attributes>
              <description>
                <description xmlns="http://cpee.org/ns/description/1.0">
                <call id="a1" endpoint="motor_start">
            <parameters>
            <label>Start Motor 1</label>
            <arguments/>
            </parameters>
            </call>
            <call id="a5" endpoint="motor_hull_sensor">
            <parameters>
            <label/>
            <arguments>
            <degree/>
            <duration/>
            </arguments>
            </parameters>
            </call>
            <loop mode="pre_test" condition="">
            <_probability>
            <_probability_min/>
            <_probability_max/>
            <_probability_avg/>
            </_probability>
            </loop>
            <call id="a2" endpoint="motor_gear">
            <parameters>
            <label/>
            <arguments>
            <level/>
            <duration/>
            </arguments>
            </parameters>
            </call>
            <call id="a3" endpoint="motor_gear">
            <parameters>
            <label/>
            <arguments>
            <level/>
            <duration/>
            </arguments>
            </parameters>
            </call>
            <call id="a4" endpoint="motor_gear">
            <parameters>
            <label/>
            <arguments>
            <level/>
            <duration/>
            </arguments>
            </parameters>
            </call>
            </description>
              </description>
              <transformation>
                <description type="copy"/>
                <dataelements type="none"/>
                <endpoints type="none"/>
              </transformation>
            </testset>""";

    /**
     * This method does explain the deviation property.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonDeviation(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info deviation: " + mouseEvent.getPickResult());
        String headline = "Deviation Information";
        String text = "The deviation describes with how much tolerance each individual sensor value (value per " +
                "second) may deviate from its ideal value in order to still be classified as \"within the norms\". " +
                "Please select the percentage in which the deviation is still accepted, for example 5%.";
        infoWindow(headline, text);
    }

    /**
     * This method does explain the acceptance property.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonAcceptance(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info acceptance: " + mouseEvent.getPickResult());
        String headline = "Acceptance Information";
        String text = "For the acceptance of a task, a certain percentage of the sensor data is allowed to lie " +
                "outside the tolerable (deviation) range. This percentage influences the acceptance of the entire " +
                "tasks and thus of the engine. Please select the percentage in which the acceptance is still valid, " +
                "for example 98%.";
        infoWindow(headline, text);
    }

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
        String text = "The customize area describes instances that are stored in this form. Your changes and " +
                "adaptations to the model are retained even after programme closure and can thus be further modified" +
                " or used again.";
        infoWindow(headline, text);
    }

    /**
     * This method does show us the information window from the new creating process.
     *
     * @param mouseEvent is the mouseclick to show the info window.
     */
    @FXML
    private void infoButtonRecreate(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - info view: " + mouseEvent.getPickResult());
        String headline = "Recreate";
        String text = "With the selection of this instance, a complete model is created with the minimum" +
                " functionality requirements.";
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
     * This Method opens the Browser with the tap of the correct modeller instance.
     *
     * @param type is the selected model.
     */
    protected int openModeller(String type) {
        // "https://cpee.org/flow/motor.html?monitor=https://cpee.org/flow/engine/21677/"
        int id = switch (type) {
            case "newView" -> post();
            case "alpha" -> 21677;
            case "beta" -> 22526;
            case "longR" -> 22539;
            default -> 22528;
        };

        String path = "https://cpee.org/flow/motor.html?monitor=https://cpee.org/flow/engine/" + id + "/";

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
        return id;
    }

    protected int post() {
        try {
            HttpClient webClient = HttpClient.newHttpClient();
            HttpRequest webRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://cpee.org/flow/start/"))
                    .setHeader("Content-Type", "application/xml")
                    .setHeader("content-id", "xml")
                    .POST(HttpRequest.BodyPublishers.ofString(webBody))
                    .build();

            HttpResponse<String> response = webClient.send(webRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String[] responseBody = response.body().split(",");
                for (String s : responseBody) {
                    if (s.startsWith("{\"CPEE-INSTANCE\":\"")) {
                        String[] values = s.split(":");
                        String id = values[1].substring(values[1].indexOf("\"") + 1, values[1].lastIndexOf("\""));
                        System.out.println(id);
                        return Integer.parseInt(id);
                    }
                }
            } else {
                System.out.println("HTTP Request failed with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.getCause();
        }
        return -1;
    }
}
