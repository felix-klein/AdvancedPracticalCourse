package edu.gate.frontend;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class ProcessController extends ComboController implements Initializable {
    @FXML
    WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webView.getEngine().load("https://cpee.org/flow/?monitor=https://cpee.org/flow/engine/21665/");
    }
}
