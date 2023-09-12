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
    final String resultScene = "/layout/ResultScene.fxml";


    /**
     * We got a two window principle, the first is the main-window (user or admin) and the second is the by-window
     * (results).
     * First User Scene:        SceneChoice=0, action=null, stage=primaryStage          - main-Window
     * Switch to User Scene:    SceneChoice=0, action=action, stage=null                - main-Window
     * Switch to Admin Scene:   SceneChoice=1, action=action, stage=null                - main-Window
     * Results Scene:           SceneChoice=2, action=action, stage=null                - by-Window
     *
     */
    public SceneControl(short sceneChoice, ActionEvent action, Stage stage) throws IOException {
        /* Switch statement to initialize the correct window in its correct position */
        switch (sceneChoice) {
            case 0 -> toScene(sceneChoice, action, stage, userScene); /* main-Window: User */
            case 1 -> toScene(sceneChoice, action, stage, adminScene); /* main-Window: Admin */
            case 2 -> toScene(sceneChoice, action, stage, resultScene);  /* by-Window: Results */
        }
    }

    /**
     * This method helps to save code duplication and does switch the scene with the according action and path.
     *
     * @param action is the Event to switch the scene.
     * @param path   is the scene to switch to.
     * @throws IOException to handle event errors.
     */
    public void toScene(short sceneChoice, ActionEvent action, Stage stageOption, String path) throws IOException {
        /* JavaFX root element for the first group of Scene-Graph elements (element grouping - low) */
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path))); /* load the fxml file */

        /* JavaFX scene including the first group for the scene creation (visualisation content - middle) */
        Scene scene = new Scene(root);

        /* JavaFX scene including the first group for the scene creation (visualisation content - middle) */
        /* If there was no stage delivered, we need to create one via the action */
        Stage stage = stageOption == null ? (Stage) ((Node) action.getSource()).getScene().getWindow() : stageOption;
        stage.setScene(scene);
        stage.setY(25.0); /* window position */
        stage.setMinHeight(200.0); /* maximum with of the window */
        if (sceneChoice<=1) {
            stage.setX(781.0); /* window position */
            stage.setMinWidth(647.0); /* minimum with of the window */
            if (sceneChoice == 0) { /* set title */
                stage.setTitle("User - Motor Quality Control Application");
            } else {
                stage.setTitle("Admin - Motor Quality Control Application");
            }
        } else {
            stage.setX(10.0); /* window position */
            stage.setMinWidth(637.0); /* minimum with of the window */
            stage.setTitle("Result Representation and Analyses"); /* set title */
        }
        stage.show();
    }
}
