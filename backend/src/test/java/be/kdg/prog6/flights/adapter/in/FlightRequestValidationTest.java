package be.kdg.prog6.flights.adapter.in;

import be.kdg.prog6.config.SecurityConfiguration;
import be.kdg.prog6.flights.port.in.ChangeDelayUseCase;
import be.kdg.prog6.flights.port.in.GetFlightQuery;
import be.kdg.prog6.flights.port.in.RecordArrivalUseCase;
import be.kdg.prog6.flights.port.in.RecordDepartureUseCase;
import be.kdg.prog6.flights.port.in.RegisterDelayUseCase;
import be.kdg.prog6.flights.port.in.RemoveDelayUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightController.class)
@Import(SecurityConfiguration.class)
class FlightRequestValidationTest {

    private static final String DELAYS = "/api/flights/HJ221/2026-11-02/BRU/delays";
    private static final SimpleGrantedAuthority FLIGHT_PLANNER = new SimpleGrantedAuthority("ROLE_flight-planner");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetFlightQuery getFlightQuery;
    @MockitoBean
    private RegisterDelayUseCase registerDelayUseCase;
    @MockitoBean
    private ChangeDelayUseCase changeDelayUseCase;
    @MockitoBean
    private RemoveDelayUseCase removeDelayUseCase;
    @MockitoBean
    private RecordDepartureUseCase recordDepartureUseCase;
    @MockitoBean
    private RecordArrivalUseCase recordArrivalUseCase;

    @Test
    void aDelayWithoutCodeAndMinutesIsRefusedWithTheMissingFields() throws Exception {
        mockMvc.perform(post(DELAYS).with(jwt().authorities(FLIGHT_PLANNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"side\": \"DEPARTURE\", \"description\": \"Late inbound aircraft\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("delayCode must not be blank; minutes must not be null"));

        verify(registerDelayUseCase, never()).registerDelay(any());
    }

    @Test
    void aBodyThatIsNotJsonIsRefused() throws Exception {
        mockMvc.perform(post(DELAYS).with(jwt().authorities(FLIGHT_PLANNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"side\": \"SIDEWAYS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The request body is not valid JSON, or a value has the wrong type"));
    }
}
