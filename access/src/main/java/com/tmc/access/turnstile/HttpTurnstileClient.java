package com.tmc.access.turnstile;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls the gate-opening endpoint of the turnstile control system over HTTP.
 * The default path and the body used here are an assumption until the real API
 * contract is available.
 */
public class HttpTurnstileClient implements TurnstileClient {
    private final RestClient restClient;
    private final String openGatePath;

    public HttpTurnstileClient(RestClient restClient, String openGatePath) {
        this.restClient = restClient;
        this.openGatePath = openGatePath;
    }

    @Override
    public void openGate(Direction direction) {
        try {
            restClient.post()
                    .uri(openGatePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("event", wireValue(direction)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new TurnstileUnavailableException("Turnstile control system did not accept the request", e);
        }
    }

    /** The value the request body carries; part of the assumed contract above. */
    private static String wireValue(Direction direction) {
        return switch (direction) {
            case ENTRY -> "CHECK_IN";
            case EXIT -> "CHECK_OUT";
        };
    }
}
