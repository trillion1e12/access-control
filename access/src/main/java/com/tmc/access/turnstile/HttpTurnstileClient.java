package com.tmc.access.turnstile;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tmc.access.attendance.AttendanceType;

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
    public void openGate(AttendanceType direction) {
        try {
            restClient.post()
                    .uri(openGatePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("event", direction.name()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new TurnstileUnavailableException("Turnstile control system did not accept the request", e);
        }
    }
}
