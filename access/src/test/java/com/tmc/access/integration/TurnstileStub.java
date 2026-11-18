package com.tmc.access.integration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpServer;

/**
 * A local HTTP server standing in for the turnstile control system. It records
 * the bodies of the gate-open requests it receives, answers with a chosen
 * status, and can be stopped and started again on the same port to simulate an
 * unreachable system.
 */
final class TurnstileStub {
    private final int port;
    private final List<String> requests = new CopyOnWriteArrayList<>();
    private volatile int status = 200;
    private HttpServer server;

    TurnstileStub() {
        this.port = freePort();
        start();
    }

    String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    List<String> requests() {
        return List.copyOf(requests);
    }

    void reset() {
        requests.clear();
        status = 200;
    }

    void answerWith(int newStatus) {
        this.status = newStatus;
    }

    void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.createContext("/gates/open", exchange -> {
            requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
    }

    void stop() {
        server.stop(0);
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
