package edu.ground.cpeeGate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class Gateway {
    private final ArrayList<String> preparedData;

    public Gateway(String type, String accuracyLevel) {
        String xmlResponse = pull(type);
        preparedData = new ProcessDataPrep(xmlResponse, accuracyLevel).getPreparedData();
    }

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

    private URI typeOpener(String type) throws URISyntaxException {
        URI returnURI;

        returnURI = switch (type) {
            case "alpha" -> new URI("https://cpee.org/flow/engine/21677/properties/");
            case "beta" -> new URI("Still blank and should be changed with option 2");
            case "gamma" -> new URI("Still blank and should be changed with option 3");
            case "longR" -> new URI("Predefined and should just display an image");
            default -> new URI("Predefined and should just display an image for the longR");
        };

        return returnURI;
    }

    public ArrayList<String> getPreparedData() {
        return preparedData;
    }
}
