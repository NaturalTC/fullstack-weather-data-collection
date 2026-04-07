package com.github.fullstackweatherdatacollectionplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  // tells Spring this class contains setup/config, read it on startup
@EnableWebSecurity  // turns on Spring Security for the whole app — without this, no security rules apply at all
public class SecurityConfig {

    // pull admin credentials from application.properties / environment variables — never hardcoded
    @Value("${admin.username}")
    private String username;

    @Value("${admin.password}")
    private String password;

    // @Bean — Spring calls this method once on startup and stores the result (manages its lifecycle)
    // SecurityFilterChain is an HTTP firewall — every request passes through it before reaching a controller
    // HttpSecurity is the builder Spring injects so we can define the rules for that firewall
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // tells Spring Security to run CORS rules (from WebConfig) BEFORE checking authentication
            // without this line, Spring Security was blocking cross-origin requests before WebConfig ever ran
            .cors(Customizer.withDefaults())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()                          // public — anyone can call these
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // public — API docs open to all
                .requestMatchers("/actuator/health").permitAll()                 // public — health check open to all
                .requestMatchers("/admin/**").authenticated()                    // protected — must be logged in
                .anyRequest().authenticated()                                    // anything else also requires login
            )

            // use HTTP Basic Auth — frontend sends username/password in the Authorization header
            .httpBasic(Customizer.withDefaults())

            // disable CSRF — this protection is for browser session/cookie apps, not stateless REST APIs
            .csrf(csrf -> csrf.disable())

            // stateless — don't create a session after login, every request must send credentials each time
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // @Bean — defines the single admin user Spring Security checks credentials against
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername(username)           // set username from env variable
                .password(encoder.encode(password))               // hash the password with BCrypt before storing — never plain text
                .roles("ADMIN")                                    // assign the ADMIN role to this user
                .build();
        return new InMemoryUserDetailsManager(admin);             // store the user in memory (no database needed for one admin)
    }

    // @Bean — defines BCrypt as the hashing algorithm used to hash and verify passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // when login happens, Spring hashes what was typed and compares to the stored hash
    }
}
