package com.tmc.access.turnstile;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TurnstileConfig {
    private static final String DEMO_PROFILE = "demo";

    /**
     * Uses the HTTP client when a base URL is configured. Without one, the logging
     * stand-in is used only under the demo profile; otherwise startup fails, so
     * that a missing setting can never pass for an opened gate.
     */
    @Bean
    public TurnstileClient turnstileClient(
            @Value("${turnstile.api.base-url:}") String baseUrl,
            @Value("${turnstile.api.open-gate-path:/gates/open}") String openGatePath,
            @Value("${turnstile.api.timeout-ms:1000}") long timeoutMs,
            Environment environment
    ) {
        if (baseUrl.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of(DEMO_PROFILE))) {
                return new LoggingTurnstileClient();
            }
            throw new IllegalStateException(
                    "turnstile.api.base-url is not set; set it, or run with the demo profile to only log requests");
        }
        Duration timeout = Duration.ofMillis(timeoutMs);
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(timeout).build()
        );
        requestFactory.setReadTimeout(timeout);
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        return new HttpTurnstileClient(restClient, openGatePath);
    }
}
