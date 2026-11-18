package com.tmc.access.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class BrokerOutageIntegrationTest extends IntegrationTestBase {
    private static final Duration FAST_ENOUGH = Duration.ofSeconds(2);

    @Test
    void scansDuringABrokerOutageAreAcceptedAndSentWhenTheBrokerReturns() {
        User first = createUser(true);
        User second = createUser(true);
        requestQrCode(first);
        requestQrCode(second);
        String firstContent = latestQrContent(first);
        String secondContent = latestQrContent(second);

        KAFKA.getDockerClient().pauseContainerCmd(KAFKA.getContainerId()).exec();
        try {
            Instant before = Instant.now();
            int firstStatus = scan(firstContent).statusCode();
            int secondStatus = scan(secondContent).statusCode();
            Duration took = Duration.between(before, Instant.now());

            assertThat(firstStatus).isEqualTo(200);
            assertThat(secondStatus).isEqualTo(200);
            assertThat(took).isLessThan(FAST_ENOUGH);
            sleep(Duration.ofSeconds(2));
            assertThat(outboxRows(first, false)).isEqualTo(1);
            assertThat(outboxRows(second, false)).isEqualTo(1);
        } finally {
            KAFKA.getDockerClient().unpauseContainerCmd(KAFKA.getContainerId()).exec();
        }

        await().atMost(Duration.ofSeconds(30)).until(() -> outboxRows(first, false) + outboxRows(second, false) == 0);
        List<String> firstMessages = messagesFor(first, Duration.ofSeconds(3));
        List<String> secondMessages = messagesFor(second, Duration.ofSeconds(3));
        assertThat(firstMessages).hasSize(1);
        assertThat(secondMessages).hasSize(1);
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
