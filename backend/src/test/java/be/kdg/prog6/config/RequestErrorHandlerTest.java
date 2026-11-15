package be.kdg.prog6.config;

import be.kdg.prog6.flights.adapter.in.FlightController;
import be.kdg.prog6.flights.port.in.ChangeDelayUseCase;
import be.kdg.prog6.flights.port.in.GetFlightQuery;
import be.kdg.prog6.flights.port.in.RecordArrivalUseCase;
import be.kdg.prog6.flights.port.in.RecordDepartureUseCase;
import be.kdg.prog6.flights.port.in.RegisterDelayUseCase;
import be.kdg.prog6.flights.port.in.RemoveDelayUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.CannotCreateTransactionException;

import java.net.ConnectException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightController.class)
@Import(SecurityConfiguration.class)
@ExtendWith(OutputCaptureExtension.class)
class RequestErrorHandlerTest {

    private static final String DELAYS = "/api/flights/HJ221/2026-11-02/BRU/delays";
    private static final String DELAY = "{\"side\": \"DEPARTURE\", \"minutes\": 20, \"delayCode\": \"93\", \"description\": \"Late inbound aircraft\"}";

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
    void changingAFlightThatDoesNotExistIsRefusedAndLogged(CapturedOutput output) throws Exception {
        doThrow(new NoSuchElementException("No flight HJ221")).when(registerDelayUseCase).registerDelay(any());

        registerDelay()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No flight HJ221"));

        assertTrue(output.getOut().contains("POST " + DELAYS + " refused: No flight HJ221"));
    }

    @Test
    void anUnreachableDatabaseIsReportedAndLogged(CapturedOutput output) throws Exception {
        doThrow(new CannotCreateTransactionException("Connection refused")).when(registerDelayUseCase).registerDelay(any());

        registerDelay()
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("The database cannot be reached at the moment"));

        assertTrue(output.getOut().contains("the database cannot be reached"));
    }

    @Test
    void anUnexpectedDatabaseErrorIsReportedAndLogged(CapturedOutput output) throws Exception {
        doThrow(new DataIntegrityViolationException("Duplicate key")).when(registerDelayUseCase).registerDelay(any());

        registerDelay()
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("The change could not be stored"));

        assertTrue(output.getOut().contains("failed with a database error"));
    }

    @Test
    void anUnreachableMessageBrokerIsReportedAndLogged(CapturedOutput output) throws Exception {
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(registerDelayUseCase).registerDelay(any());

        registerDelay()
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("The message broker cannot be reached at the moment"));

        assertTrue(output.getOut().contains("the message broker cannot be reached"));
    }

    private ResultActions registerDelay() throws Exception {
        return mockMvc.perform(post(DELAYS)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_flight-planner")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(DELAY));
    }
}
