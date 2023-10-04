package edu.ground.cpeeGate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Objects;

public class Gateway {
    private final ArrayList<String> preparedData;

    /**
     * The Gateway is the "gateway" for the model data to the datapreparation for the hardware.
     *
     * @param type is the model we need to prepare for the Hardware.
     * @param accuracyLevel is an additional parameter for the accuracy of the sensor data.
     */
    public Gateway(int type, String accuracyLevel) {
        String xmlResponse = pull(type);
        preparedData = new ProcessDataPrep(xmlResponse, accuracyLevel).getPreparedData();
    }

    /**
     * This method pull the xml data from the chosen type and saves it in a single string.
     *
     * @param type is the model we were ask to use.
     * @return a single String including all the xml data with linebreaks.
     */
    private String pull(int type) {
        try {
            URI uriLog = new URI("https://cpee.org/flow/engine/" + type + "/properties/");

            HttpClient webClient = HttpClient.newHttpClient();
            HttpRequest webRequest = HttpRequest.newBuilder().uri(uriLog).GET().build();

            HttpResponse<String> response = webClient.send(webRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.out.println("HTTP Request failed with status code: " + response.statusCode());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.getCause();
        }
        return null;
    }

    /**
     * The Getter for the Prepared Data returns the final List of Strings for the Hardware in its correct formatting.
     *
     * @return a List of Strings, where each line represents a Command.
     */
    public ArrayList<String> getPreparedData() {
        return preparedData;
    }
}
