package edu.gate.frontend;

import edu.ground.analysis.ComplianceResults;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ResultController extends ComboController implements Initializable {
    @FXML
    private Label overallComplianceLabel, compliantTasksLabel, complianceRateLabel;
    @FXML
    private Label tasksLabel, accuracyLabel, deviationLabel, acceptanceLabel, missionLabel;
    @FXML
    private BarChart<String, Double> accuracyBarChart;

    @FXML
    private ChoiceBox<String> sensor;

    @FXML
    private LineChart<String, Double> deviationChart;

    XYChart.Series<String, Double> tmp = new XYChart.Series<>();
    XYChart.Series<String, Double> vib = new XYChart.Series<>();
    XYChart.Series<String, Double> mic = new XYChart.Series<>();
    XYChart.Series<String, Double> cp1 = new XYChart.Series<>();
    XYChart.Series<String, Double> cp2 = new XYChart.Series<>();
    XYChart.Series<String, Double> cp3 = new XYChart.Series<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* Getting the compliance results from the MainApplication. */
        ComplianceResults results = MainApplication.complianceResults;

        /* Initialize all labels. */
        initializeLabels(results);

        /* Initialize the bar char. */
        initializeBarChart(results);

        /* Calculate the Line Chart values. */
        deviationChartCalc(results);
        System.out.println("Test2");

        /* Infrastructure and main initialisation of the Line Chart. */
        String[] sensors = new String[]{"Temperature", "Vibration", "Loudness", "Current1", "Current2", "Current3"};
        sensor.getItems().addAll(sensors);
        sensor.setValue("Temperature"); /* default */
        initializeLineChart();
    }

    /**
     * This helper method is for code clarity and initializes all labels.
     *
     * @param results are the compliance test results.
     */
    private void initializeLabels(ComplianceResults results) {
        /* Initialize all labels. */
        overallComplianceLabel.setText(String.valueOf(results.getOverallCompliance()));
        compliantTasksLabel.setText(String.valueOf(results.getCompliantTasks()));
        complianceRateLabel.setText(results.getComplianceRate() + " %");
        tasksLabel.setText(String.valueOf(results.getTasks()));
        accuracyLabel.setText(results.getAccuracy());
        deviationLabel.setText(results.getDeviation() + " %");
        acceptanceLabel.setText(results.getAcceptance() + " %");

        /* Area and loop for the initialisation of the mission list. */
        String missionList = "";
        for (int i = 0; i < results.getMissionTotals().size(); i++) {
            int indexCut = results.getMissionTotals().get(i).mission().indexOf("&tsp=");
            String mission = i + ": \"" + results.getMissionTotals().get(i).mission().substring(0, indexCut) + "\"";
            if(i == results.getMissionTotals().size() - 1) {
                missionLabel.setText(missionList + mission);
            } else {
                missionList += mission + "; ";
            }
        }
    }

    /**
     * This helper method is for code clarification and initializes the bar chart
     *
     * @param results are the compliance test results.
     */
    private void initializeBarChart(ComplianceResults results) {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        series.setName("Compliance Rate");

        for (int i = 0; i < results.getMissionTotals().size(); i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i), results.getMissionTotals().get(i).percentage()));
        }

        accuracyBarChart.getData().add(series);
    }

    private void initializeLineChart() {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        series.setName("Deviation Course");

        if (sensor.getValue().equals("Temperature")) {
            deviationChart.getData().add(tmp);
        } else if (sensor.getValue().equals("Vibration")) {
            deviationChart.getData().add(vib);
        } else if (sensor.getValue().equals("Loudness")) {
            deviationChart.getData().add(mic);
        } else if (sensor.getValue().equals("Current1")) {
            deviationChart.getData().add(cp1);
        } else if (sensor.getValue().equals("Current2")) {
            deviationChart.getData().add(cp2);
        } else {
            deviationChart.getData().add(cp3);
        }
    }

    private void deviationChartCalc(ComplianceResults results) {
        for (int i = 0; i < results.getMissionTotals().size(); i++) {
            double tmpDeviation = 0.0;
            double division;

            for (int m = 0; m < results.getDataTMP().get(i).size(); m++) {
                tmpDeviation += results.getDataTMP().get(i).get(m).deviation();
            }
            division = results.getDataTMP().get(i).isEmpty() ? 0 : (tmpDeviation / results.getDataTMP().get(i).size());
            tmp.getData().add(new XYChart.Data<>(String.valueOf(i), division));

            double vibDeviation = 0.0;
            for (int m = 0; m < results.getDataVIB().get(i).size(); m++) {
                vibDeviation += results.getDataVIB().get(i).get(m).deviation();
            }
            division = results.getDataVIB().get(i).isEmpty() ? 0 : (vibDeviation / results.getDataVIB().get(i).size());
            vib.getData().add(new XYChart.Data<>(String.valueOf(i), division));

            double micDeviation = 0.0;
            for (int m = 0; m < results.getDataMIC().get(i).size(); m++) {
                micDeviation += results.getDataMIC().get(i).get(m).deviation();
            }
            division = results.getDataMIC().get(i).isEmpty() ? 0 : (micDeviation / results.getDataMIC().get(i).size());
            mic.getData().add(new XYChart.Data<>(String.valueOf(i), division));

            double cp1Deviation = 0.0;
            for (int m = 0; m < results.getDataCP1().get(i).size(); m++) {
                cp1Deviation += results.getDataCP1().get(i).get(m).deviation();
            }
            division = results.getDataCP1().get(i).isEmpty() ? 0 : (cp1Deviation / results.getDataCP1().get(i).size());
            cp1.getData().add(new XYChart.Data<>(String.valueOf(i), division));

            double cp2Deviation = 0.0;
            for (int m = 0; m < results.getDataCP2().get(i).size(); m++) {
                cp2Deviation += results.getDataCP2().get(i).get(m).deviation();
            }
            division = results.getDataCP2().get(i).isEmpty() ? 0 : (cp2Deviation / results.getDataCP2().get(i).size());
            cp2.getData().add(new XYChart.Data<>(String.valueOf(i), division));

            double cp3Deviation = 0.0;
            for (int m = 0; m < results.getDataCP3().get(i).size(); m++) {
                cp3Deviation += results.getDataCP3().get(i).get(m).deviation();
            }
            division = results.getDataCP3().get(i).isEmpty() ? 0 : (cp3Deviation / results.getDataCP3().get(i).size());
            cp3.getData().add(new XYChart.Data<>(String.valueOf(i), division));
        }
    }

    /**
     * This method actualizes the line chart via mouse release.
     *
     * @param mouseEvent is the event to open the process modeler.
     */
    @FXML
    private void chosen(javafx.scene.input.MouseEvent mouseEvent) {
        System.out.println("Button - Chosen: " + mouseEvent.getPickResult());
        initializeLineChart();
    }
}
