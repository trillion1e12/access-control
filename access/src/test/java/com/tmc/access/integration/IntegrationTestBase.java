package com.tmc.access.integration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Starts the service against a real PostgreSQL and a real Kafka, each in a
 * container, and gives the tests helpers to call it over HTTP. The containers
 * are started once for all test classes, which also share one application
 * context so that a single outbox relay runs.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTestBase {
    static final String TOPIC = "attendance";

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18");
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.3.1");
    static final TurnstileStub TURNSTILE = new TurnstileStub();

    private static final RSAKey SIGNING_KEY;
    private static final Path PUBLIC_KEY_FILE;
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    static {
        POSTGRES.start();
        KAFKA.start();
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            SIGNING_KEY = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .build();
            String pem = "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                            .encodeToString(keyPair.getPublic().getEncoded())
                    + "\n-----END PUBLIC KEY-----\n";
            PUBLIC_KEY_FILE = Files.createTempFile("access-test-public", ".key");
            Files.writeString(PUBLIC_KEY_FILE, pem);
            PUBLIC_KEY_FILE.toFile().deleteOnExit();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.producer.acks", () -> "all");
        registry.add("spring.kafka.producer.properties.max.block.ms", () -> "1000");
        registry.add("app.outbox.poll-interval-ms", () -> "200");
        registry.add("jwt.public.key", () -> "file:" + PUBLIC_KEY_FILE);
        registry.add("turnstile.api.base-url", TURNSTILE::baseUrl);
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Value("${local.server.port}")
    private int port;

    @BeforeEach
    void resetTurnstile() {
        TURNSTILE.reset();
    }

    protected User createUser(boolean active) {
        User user = new User(UUID.randomUUID(), "user-" + UUID.randomUUID() + "@tmc.test");
        jdbcTemplate.update("insert into users (id, email, active) values (?, ?, ?)", user.id(), user.email(), active);
        return user;
    }

    /** Asks for a QR code the way an employee does, with a JWT. */
    protected HttpResponse<byte[]> requestQrCode(User user) {
        return send(request("/qr-codes").header("Authorization", "Bearer " + jwtFor(user)).GET().build());
    }

    protected String latestQrContent(User user) {
        return jdbcTemplate.queryForObject(
                "select content from qr_codes where user_id = ? order by created_at desc limit 1",
                String.class, user.id());
    }

    /**
     * Scans a QR code the way a kiosk does: no JWT, and the proxy has verified a
     * client certificate.
     */
    protected HttpResponse<byte[]> scan(String content) {
        return scan(content, true);
    }

    protected HttpResponse<byte[]> scan(String content, boolean clientCertificateVerified) {
        HttpRequest.Builder builder = request("/qr-codes")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"content\":\"" + content + "\",\"event\":\"CHECK_IN\"}"));
        if (clientCertificateVerified) {
            builder.header("X-Client-Verify", "SUCCESS");
        }
        return send(builder.build());
    }

    protected HttpResponse<byte[]> get(String path) {
        return send(request(path).GET().build());
    }

    protected static String text(HttpResponse<byte[]> response) {
        return new String(response.body(), StandardCharsets.UTF_8);
    }

    protected int outboxRows(User user, boolean published) {
        String condition = published ? "published_at is not null" : "published_at is null";
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from attendance_outbox where user_id = ? and " + condition,
                Integer.class, user.id());
        return count == null ? 0 : count;
    }

    protected boolean qrCodeStillValid(String content) {
        Boolean valid = jdbcTemplate.queryForObject(
                "select valid from qr_codes where content = ?", Boolean.class, content);
        return Boolean.TRUE.equals(valid);
    }

    /**
     * Reads the attendance topic from the start and returns the messages sent for
     * the user.
     */
    protected List<String> messagesFor(User user, Duration listenFor) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        List<String> found = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config)) {
            consumer.subscribe(List.of(TOPIC));
            Instant until = Instant.now().plus(listenFor);
            while (Instant.now().isBefore(until)) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(300))) {
                    if (user.id().toString().equals(record.key())) {
                        found.add(record.value());
                    }
                }
            }
        }
        return found;
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(10));
    }

    private HttpResponse<byte[]> send(HttpRequest request) {
        try {
            return HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static String jwtFor(User user) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(SIGNING_KEY)));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.email())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    protected record User(UUID id, String email) {
    }
}
