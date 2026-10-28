package com.tmc.attendance.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.tmc.attendance.user.UserRole;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // @Bean
    // public UserDetailsService userDetailsService() {
    // UserDetails user1 = User.withUsername("tri")
    // .password(passwordEncoder().encode("password"))
    // .roles("USER")
    // .build();
    // return new InMemoryUserDetailsManager(user1);
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .authorizeHttpRequests(matcherRegistry -> matcherRegistry
                        .requestMatchers("/users").hasAnyRole(UserRole.ADMIN.toString())
                        .anyRequest().authenticated())
                // .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .csrf(csrfConfigurer -> csrfConfigurer.disable())
                .build();

    }
}
