package be.kdg.prog6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    static final String FLIGHT_PLANNER = "flight-planner";
    static final String LOAD_PLANNER = "load-planner";
    static final String ADMIN = "admin";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers("/api/flights/*/*/*/delays/**", "/api/flights/*/*/*/departure",
                                "/api/flights/*/*/*/arrival").hasRole(FLIGHT_PLANNER)
                        .requestMatchers("/api/loads/*/*/*/positions/**", "/api/loads/*/*/*/final")
                        .hasRole(LOAD_PLANNER)
                        .requestMatchers(HttpMethod.PUT, "/api/policy-settings/**").hasRole(ADMIN)
                        .requestMatchers("/api/simulator/**").hasRole(ADMIN)
                        .anyRequest().denyAll())
                .oauth2ResourceServer(server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakAuthentication())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .build();
    }

    private static JwtAuthenticationConverter keycloakAuthentication() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
