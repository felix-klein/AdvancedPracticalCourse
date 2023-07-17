package edu.gate.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import com.fazecast.jSerialComm.SerialPort;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * The AdminController is the controller class for all events from the AdminScene.fxml.
 */
public class AdminController extends ComboController implements Initializable {
    @FXML
    CheckBox check1, check2, check3, check4, check5, check6, check7;
    @FXML
    ChoiceBox<String> PortDown, BaudRateDown;

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

    /**
     * Action method for the saving of the desired Port.
     *
     * @param action is the trigger to save the choice.
     */
    @FXML
    private void setupDataInitializerPort(ActionEvent action) {
        newDataReplacer("Port:", PortDown.getValue());
    }

    /**
     * Action method fo the saving of the desired Baud Rate.
     * @param action is the trigger to save the choice.
     */
    @FXML
    private void setupDataInitializerBaud(ActionEvent action) {
        newDataReplacer("BaudRate:", BaudRateDown.getValue());
    }

    /**
     * Method for the Drop-Down menus in the Port Specification area of the admin page.
     *
     * @param url is an initialize parameter from Initializable.
     * @param resourceBundle is an initialize parameter from Initializable.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* This array and the corresponding loop are searching and saving the currently available loops. */
        ArrayList<String> allPorts = new ArrayList<>();
        for(int i=0; i<SerialPort.getCommPorts().length; i++) {
            if(!SerialPort.getCommPorts()[i].toString().contains("(Dial-In)")) {
                allPorts.add(SerialPort.getCommPorts()[i].getSystemPortPath());
            }
        }

        /* To initialize the Drop-Down options. */
        PortDown.getItems().addAll(allPorts);
        PortDown.setOnAction(this::setupDataInitializerPort);
        BaudRateDown.getItems().addAll("115200", "9600", "19200", "38400", "57600");
        BaudRateDown.setOnAction(this::setupDataInitializerBaud);
    }

    /**
     * This helper method finds the line to change and changes it in teh setup data file.
     *
     * @param dataHeader is the name of the data to change.
     * @param newData is the new data for this element.
     */
    private void newDataReplacer(String dataHeader, String newData) {
        /*
         * Every change will be visible in the target version of the setupData.txt file, not in the file of this
         * resources' folder! This setupData.txt in resources is the default and original storage.
         */
        try {
            /* Indicate the right path in the correct format. */
            Path fullPath = Paths.get(Objects.requireNonNull(getClass().getResource("/data/setupData.txt"))
                    .toURI());
            /* Iterate through all lines of the document and change the one line with its new data. */
            List<String> setupSheet = Files.readAllLines(fullPath);
            for(int i=0; i<setupSheet.size(); i++) {
                if (setupSheet.get(i).contains(dataHeader)) {
                    setupSheet.set(i, (dataHeader + newData + '*'));
                    System.out.println(setupSheet.get(i));
                    System.out.println(fullPath);
                }
            }
            /* Write the new file including the new data. */
            Files.write(fullPath, setupSheet, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (URISyntaxException | IOException e) {
            System.out.println("Error in the data update section of the setup data.");
            e.printStackTrace();
        }
    }
}
