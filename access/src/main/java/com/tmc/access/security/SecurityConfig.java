package com.tmc.access.security;

import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
public class SecurityConfig {
    private static final String CLIENT_VERIFY_HEADER = "X-Client-Verify";
    private static final String CLIENT_VERIFIED = "SUCCESS";

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Grants a request only when the TLS-terminating proxy reports that the caller
     * presented a client certificate it could verify. The service must therefore be
     * reachable only through that proxy.
     */
    private static AuthorizationManager<RequestAuthorizationContext> verifiedClientCertificate() {
        return (authentication, context) -> new AuthorizationDecision(
                CLIENT_VERIFIED.equals(context.getRequest().getHeader(CLIENT_VERIFY_HEADER)));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/qr-codes").access(verifiedClientCertificate())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrfConfigurer -> csrfConfigurer.disable())
                .build();
    }
}
