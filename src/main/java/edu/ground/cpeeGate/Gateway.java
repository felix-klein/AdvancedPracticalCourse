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
    public Gateway(String type, String accuracyLevel) {
        String xmlResponse = pull(type);
        preparedData = new ProcessDataPrep(xmlResponse, accuracyLevel).getPreparedData();
    }

    /**
     * This method pull the xml data from the chosen type and saves it in a single string.
     *
     * @param type is the model we were ask to use.
     * @return a single String including all the xml data with linebreaks.
     */
    private String pull(String type) {
        try {
            URI uriLog = typeOpener(type);

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
     * This helper method compares the gotten "type" and connects it its correct URI.
     *
     * @param type is the model we were ask to use and of which we need to find the corresponding URI.
     * @return a URI, where we can GET the xml data from.
     * @throws URISyntaxException if the URI is incorrect.
     */
    private URI typeOpener(String type) throws URISyntaxException {
        URI returnURI;

        returnURI = switch (type) {
            case "blueprint" -> Objects.requireNonNull(getClass().getResource("/adminFiles/setup.bpmn")).toURI();
            case "alpha" -> new URI("https://cpee.org/flow/engine/21677/properties/");
            case "beta" -> new URI("Still blank and should be changed with option 2");
            case "gamma" -> new URI("Still blank and should be changed with option 3");
            case "longR" -> new URI("Predefined and should just display an image");
            default -> new URI("Predefined and should just display an image for the longR");
        };

        return returnURI;
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
