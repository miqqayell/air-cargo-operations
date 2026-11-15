package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.config.SecurityConfiguration;
import be.kdg.prog6.flights.domain.FlightPolicy;
import be.kdg.prog6.flights.port.in.GetFlightPolicyQuery;
import be.kdg.prog6.flights.port.in.UpdateFlightPolicyCommand;
import be.kdg.prog6.flights.port.in.UpdateFlightPolicyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightPolicyController.class)
@Import(SecurityConfiguration.class)
class FlightPolicyControllerTest {

    private static final String SETTINGS = "/api/policy-settings/flights";
    private static final SimpleGrantedAuthority FLIGHT_PLANNER = new SimpleGrantedAuthority("ROLE_flight-planner");
    private static final SimpleGrantedAuthority LOAD_PLANNER = new SimpleGrantedAuthority("ROLE_load-planner");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_admin");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetFlightPolicyQuery getFlightPolicyQuery;
    @MockitoBean
    private UpdateFlightPolicyUseCase updateFlightPolicyUseCase;

    @BeforeEach
    void currentPolicy() {
        when(getFlightPolicyQuery.getFlightPolicy()).thenReturn(new FlightPolicy(Duration.ofMinutes(5), Duration.ofDays(7)));
        when(updateFlightPolicyUseCase.updateFlightPolicy(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, UpdateFlightPolicyCommand.class).flightPolicy());
    }

    @Test
    void everyRoleReadsTheSettings() throws Exception {
        for (var role : new SimpleGrantedAuthority[]{FLIGHT_PLANNER, LOAD_PLANNER, ADMIN}) {
            mockMvc.perform(get(SETTINGS).with(jwt().authorities(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.actualTimeToleranceMinutes").value(5))
                    .andExpect(jsonPath("$.retentionDays").value(7));
        }
    }

    @Test
    void anAdminChangesTheSettings() throws Exception {
        mockMvc.perform(update("{\"actualTimeToleranceMinutes\": 10, \"retentionDays\": 30}").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualTimeToleranceMinutes").value(10))
                .andExpect(jsonPath("$.retentionDays").value(30));

        verify(updateFlightPolicyUseCase).updateFlightPolicy(
                new UpdateFlightPolicyCommand(new FlightPolicy(Duration.ofMinutes(10), Duration.ofDays(30))));
    }

    @Test
    void thePlannersMayNotChangeTheSettings() throws Exception {
        String body = "{\"actualTimeToleranceMinutes\": 60, \"retentionDays\": 30}";
        mockMvc.perform(update(body).with(jwt().authorities(FLIGHT_PLANNER))).andExpect(status().isForbidden());
        mockMvc.perform(update(body).with(jwt().authorities(LOAD_PLANNER))).andExpect(status().isForbidden());
        mockMvc.perform(update(body)).andExpect(status().isUnauthorized());

        verify(updateFlightPolicyUseCase, never()).updateFlightPolicy(any());
    }

    @Test
    void invalidValuesAreRefusedWithAClearMessage() throws Exception {
        mockMvc.perform(update("{\"actualTimeToleranceMinutes\": -5, \"retentionDays\": 7}").with(jwt().authorities(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The actual-time tolerance must be between 0 and 60 minutes"));
        mockMvc.perform(update("{\"actualTimeToleranceMinutes\": 5}").with(jwt().authorities(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("retentionDays must not be null"));

        verify(updateFlightPolicyUseCase, never()).updateFlightPolicy(any());
    }

    private static MockHttpServletRequestBuilder update(String body) {
        return put(SETTINGS).contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
