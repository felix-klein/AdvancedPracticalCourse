package edu.gate.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * The Class SceneControl allows us to switch between the User-Scene and the Admin-Scene via a button and also create
 * the User-Scene for the first time at initialisation.
 */
public class SceneControl {
    final String userScene = "/layout/UserScene.fxml";
    final String adminScene = "/layout/AdminScene.fxml";

    /**
     * Initialisation Info:
     * Main Window - User: toUserScene=true, action=null, stage=primaryStage
     * Switch to User: toUserScene=true, action=action, stage=null
     * Switch to Admin: toUserScene=false, action=action, stage=null
     */
    public SceneControl(boolean toUserScene, ActionEvent action, Stage stage) throws IOException {
        /* If else statement to decide if we want to initialise the user or admin page */
        if (toUserScene) {
            /* If it is the creation of the main page, the action value will be null */
            toScene(action, stage, userScene); /* User-Page or First-User-Page */
        } else {
            /* In this case, the 'stage' parameter will always be null. */
            toScene(action, null, adminScene); /* Admin-Page */
        }
    }

    /**
     * This method helps to save code duplication and does switch the scene with the according action and path.
     * @param action is the Event to switch the scene.
     * @param path is the scene to switch to.
     * @throws IOException to handle event errors.
     */
    public void toScene(ActionEvent action, Stage stageOption, String path) throws IOException {
        /* JavaFX root element for the first group of Scene-Graph elements (element grouping - low) */
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path))); /* load the fxml file */

        /* JavaFX scene including the first group for the scene creation (visualisation content - middle) */
        Scene scene = new Scene(root);

        /* JavaFX scene including the first group for the scene creation (visualisation content - middle) */
        /* If there was no stage delivered, we need to create one via the action */
        Stage stage = stageOption==null ? (Stage) ((Node) action.getSource()).getScene().getWindow() : stageOption;
        stage.setScene(scene);
        stage.setX(781.0); /* window position */
        stage.setY(25.0); /* window position */
        stage.setMinWidth(647.0); /* minimum with of the window */
        stage.setMinHeight(200.0); /* maximum with of the window */
        stage.setTitle("Motor Quality Control Application"); /* set title */
        stage.show();
    }
}
