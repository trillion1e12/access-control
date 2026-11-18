package com.tmc.access.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

class ScanFlowIntegrationTest extends IntegrationTestBase {
    @Test
    void acceptedScanOpensTheGateAndDeliversOneMessage() {
        User user = createUser(true);

        HttpResponse<byte[]> qr = requestQrCode(user);
        assertThat(qr.statusCode()).isEqualTo(200);
        assertThat(qr.headers().firstValue("Content-Type")).hasValue("image/png");
        assertThat(qr.body()).startsWith((byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G');

        HttpResponse<byte[]> scan = scan(latestQrContent(user));

        assertThat(scan.statusCode()).isEqualTo(200);
        assertThat(text(scan)).isEqualTo("Access granted");
        assertThat(TURNSTILE.requests()).hasSize(1);
        assertThat(TURNSTILE.requests().get(0)).contains("CHECK_IN");
        await().atMost(Duration.ofSeconds(15)).until(() -> outboxRows(user, true) == 1);
        List<String> messages = messagesFor(user, Duration.ofSeconds(3));
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).contains("CHECK_IN").contains(user.id().toString());
    }

    @Test
    void qrCodeCanBeUsedOnlyOnce() {
        User user = createUser(true);
        requestQrCode(user);
        String content = latestQrContent(user);

        assertThat(scan(content).statusCode()).isEqualTo(200);
        HttpResponse<byte[]> second = scan(content);

        assertThat(second.statusCode()).isEqualTo(401);
        assertThat(text(second)).isEqualTo("QR code has already been used");
        assertThat(TURNSTILE.requests()).hasSize(1);
    }

    @Test
    void concurrentScansOfOneQrCodeAcceptExactlyOne() throws Exception {
        User user = createUser(true);
        requestQrCode(user);
        String content = latestQrContent(user);
        int scans = 10;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(scans);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < scans; i++) {
                Callable<Integer> task = () -> {
                    start.await();
                    return scan(content).statusCode();
                };
                results.add(executor.submit(task));
            }
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> result : results) {
                statuses.add(result.get());
            }

            assertThat(statuses).filteredOn(status -> status == 200).hasSize(1);
            assertThat(statuses).filteredOn(status -> status == 401).hasSize(scans - 1);
        } finally {
            executor.shutdownNow();
        }
        assertThat(TURNSTILE.requests()).hasSize(1);
        assertThat(outboxRows(user, true) + outboxRows(user, false)).isEqualTo(1);
    }

    @Test
    void unknownQrCodeIsRejected() {
        assertThat(scan("not-a-known-token").statusCode()).isEqualTo(401);
        assertThat(TURNSTILE.requests()).isEmpty();
    }

    @Test
    void expiredQrCodeIsRejected() {
        User user = createUser(true);
        requestQrCode(user);
        String content = latestQrContent(user);
        jdbcTemplate.update("update qr_codes set expires_at = now() - interval '1 second' where content = ?", content);

        HttpResponse<byte[]> scan = scan(content);

        assertThat(scan.statusCode()).isEqualTo(401);
        assertThat(text(scan)).isEqualTo("QR code has expired");
        assertThat(TURNSTILE.requests()).isEmpty();
    }

    @Test
    void userDeactivatedAfterGettingTheQrCodeIsRejected() {
        User user = createUser(true);
        requestQrCode(user);
        String content = latestQrContent(user);
        jdbcTemplate.update("update users set active = false where id = ?", user.id());

        assertThat(scan(content).statusCode()).isEqualTo(401);
        assertThat(TURNSTILE.requests()).isEmpty();
    }

    @Test
    void scanWithoutVerifiedClientCertificateIsRejectedAndKeepsTheQrCode() {
        User user = createUser(true);
        requestQrCode(user);
        String content = latestQrContent(user);

        assertThat(scan(content, false).statusCode()).isEqualTo(401);
        assertThat(qrCodeStillValid(content)).isTrue();
        assertThat(scan(content, true).statusCode()).isEqualTo(200);
    }

    @Test
    void inactiveUserGetsNoQrCode() {
        User user = createUser(false);

        assertThat(requestQrCode(user).statusCode()).isEqualTo(403);
    }

    @Test
    void qrCodeRequestWithoutJwtIsRejected() {
        assertThat(get("/qr-codes").statusCode()).isEqualTo(401);
    }

    @Test
    void healthEndpointNeedsNoJwtButOtherActuatorEndpointsDo() {
        assertThat(get("/actuator/health/readiness").statusCode()).isEqualTo(200);
        assertThat(get("/actuator/env").statusCode()).isEqualTo(401);
    }

    @Test
    void gateRefusalKeepsTheQrCodeUsableAndStoresNoEvent() {
        User user = createUser(true);
        requestQrCode(user);
        String content = latestQrContent(user);
        TURNSTILE.answerWith(500);

        HttpResponse<byte[]> refused = scan(content);

        assertThat(refused.statusCode()).isEqualTo(503);
        assertThat(qrCodeStillValid(content)).isTrue();
        assertThat(outboxRows(user, true) + outboxRows(user, false)).isZero();

        TURNSTILE.answerWith(200);
        assertThat(scan(content).statusCode()).isEqualTo(200);
    }

    @Test
    void unreachableGateKeepsTheQrCodeUsableAndStoresNoEvent() {
        User user = createUser(true);
        requestQrCode(user);
        String content = latestQrContent(user);
        TURNSTILE.stop();
        try {
            HttpResponse<byte[]> refused = scan(content);

            assertThat(refused.statusCode()).isEqualTo(503);
            assertThat(qrCodeStillValid(content)).isTrue();
            assertThat(outboxRows(user, true) + outboxRows(user, false)).isZero();
        } finally {
            TURNSTILE.start();
        }
        assertThat(scan(content).statusCode()).isEqualTo(200);
    }
}
