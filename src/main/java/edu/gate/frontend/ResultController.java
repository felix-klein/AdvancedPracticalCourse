package edu.gate.frontend;

import edu.ground.analysis.ComplianceResults;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class ResultController extends ComboController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ComplianceResults complianceResults = MainApplication.getTerminal().getComplianceResults();
        System.out.println(complianceResults.getIdealNoise().TMP());
    }
}
