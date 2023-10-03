package edu.ground.analysis;

import java.util.ArrayList;

public class ComplianceResults {
    private ArrayList<String> missions;
    private ArrayList<Boolean> results;
    private ArrayList<ArrayList<DataList>> dataTMP;
    private ArrayList<ArrayList<DataList>> dataVIB;
    private ArrayList<ArrayList<DataList>> dataMIC;
    private ArrayList<ArrayList<DataList>> dataCP1;
    private ArrayList<ArrayList<DataList>> dataCP2;
    private ArrayList<ArrayList<DataList>> dataCP3;
    private Noise idealNoise;
    private Noise testNoise;


    public ComplianceResults() {
        this.missions = new ArrayList<>();
        this.results = new ArrayList<>();
        this.dataTMP = new ArrayList<>();
        this.dataVIB = new ArrayList<>();
        this.dataMIC = new ArrayList<>();
        this.dataCP1 = new ArrayList<>();
        this.dataCP2 = new ArrayList<>();
        this.dataCP3 = new ArrayList<>();
    }

    public record DataList(double idealR, double testR, double diffRelative, double diffAbsolute,
                           double idealA, double testA, double deviation) {}

    public record Noise(double TMP, double VIB, double MIC, double CP1, double CP2, double CP3) {}

    public ArrayList<String> getMissions() {
        return missions;
    }

    public ArrayList<Boolean> getResults() {
        return results;
    }

    public ArrayList<ArrayList<DataList>> getDataTMP() {
        return dataTMP;
    }

    public ArrayList<ArrayList<DataList>> getDataVIB() {
        return dataVIB;
    }

    public ArrayList<ArrayList<DataList>> getDataMIC() {
        return dataMIC;
    }

    public ArrayList<ArrayList<DataList>> getDataCP1() {
        return dataCP1;
    }

    public ArrayList<ArrayList<DataList>> getDataCP2() {
        return dataCP2;
    }

    public ArrayList<ArrayList<DataList>> getDataCP3() {
        return dataCP3;
    }

    public Noise getIdealNoise() {
        return idealNoise;
    }

    public Noise getTestNoise() {
        return testNoise;
    }

    public void setIdealNoise(Noise idealNoise) {
        this.idealNoise = idealNoise;
    }

    public void setTestNoise(Noise testNoise) {
        this.testNoise = testNoise;
    }

    /**
     * Modified setter for the mission to add a mission and not to initialize a complete mission set.
     * @param mission is a single mission.
     */
    public void setMissions(String mission) {
        this.missions.add(mission);
    }

    /**
     * Modified setter for the results to add a result and not to initialize a complete results set.
     * @param result is a single mission result.
     */
    public void setResults(Boolean result) {
        this.results.add(result);
    }

    /**
     * Modified setter for the DataList adding to the list.
     * @param dataTMP is a single DataList object we add.
     */
    public void setDataTMP(ArrayList<DataList> dataTMP) {
        this.dataTMP.add(dataTMP);
    }

    /**
     * Modified setter for the DataList adding to the list.
     * @param dataVIB is a single DataList object we add.
     */
    public void setDataVIB(ArrayList<DataList> dataVIB) {
        this.dataVIB.add(dataVIB);
    }

    /**
     * Modified setter for the DataList adding to the list.
     * @param dataMIC is a single DataList object we add.
     */
    public void setDataMIC(ArrayList<DataList> dataMIC) {
        this.dataMIC.add(dataMIC);
    }

    /**
     * Modified setter for the DataList adding to the list.
     * @param dataCP1 is a single DataList object we add.
     */
    public void setDataCP1(ArrayList<DataList> dataCP1) {
        this.dataCP1.add(dataCP1);
    }

    /**
     * Modified setter for the DataList adding to the list.
     * @param dataCP2 is a single DataList object we add.
     */
    public void setDataCP2(ArrayList<DataList> dataCP2) {
        this.dataCP2.add(dataCP2);
    }

    /**
     * Modified setter for the DataList adding to the list.
     * @param dataCP3 is a single DataList object we add.
     */
    public void setDataCP3(ArrayList<DataList> dataCP3) {
        this.dataCP3.add(dataCP3);
    }
}


