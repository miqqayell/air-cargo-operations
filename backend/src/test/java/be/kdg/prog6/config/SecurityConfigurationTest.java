package be.kdg.prog6.config;

import be.kdg.prog6.flights.adapter.in.DelayCodeController;
import be.kdg.prog6.flights.adapter.in.FlightController;
import be.kdg.prog6.flights.adapter.in.FlightSearchController;
import be.kdg.prog6.flights.port.in.ChangeDelayUseCase;
import be.kdg.prog6.flights.port.in.GetFlightQuery;
import be.kdg.prog6.flights.port.in.ListDelayCodesQuery;
import be.kdg.prog6.flights.port.in.ListFlightsQuery;
import be.kdg.prog6.flights.port.in.RecordArrivalUseCase;
import be.kdg.prog6.flights.port.in.RecordDepartureUseCase;
import be.kdg.prog6.flights.port.in.RegisterDelayUseCase;
import be.kdg.prog6.flights.port.in.RemoveDelayUseCase;
import be.kdg.prog6.loading.adapter.in.LoadController;
import be.kdg.prog6.loading.port.in.AssignPositionUseCase;
import be.kdg.prog6.loading.port.in.CheckLoadQuery;
import be.kdg.prog6.loading.port.in.DeclareLoadFinalUseCase;
import be.kdg.prog6.loading.port.in.GetLoadQuery;
import be.kdg.prog6.loading.port.in.TakeOffDeckUseCase;
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

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FlightController.class, FlightSearchController.class, DelayCodeController.class,
        LoadController.class})
@Import(SecurityConfiguration.class)
class SecurityConfigurationTest {

    private static final String FLIGHT = "/api/flights/HJ221/2026-11-02/BRU";
    private static final String LOAD = "/api/loads/HJ221/2026-11-02/BRU";
    private static final SimpleGrantedAuthority FLIGHT_PLANNER = new SimpleGrantedAuthority("ROLE_flight-planner");
    private static final SimpleGrantedAuthority LOAD_PLANNER = new SimpleGrantedAuthority("ROLE_load-planner");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_admin");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetFlightQuery getFlightQuery;
    @MockitoBean
    private ListFlightsQuery listFlightsQuery;
    @MockitoBean
    private ListDelayCodesQuery listDelayCodesQuery;
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
    @MockitoBean
    private GetLoadQuery getLoadQuery;
    @MockitoBean
    private CheckLoadQuery checkLoadQuery;
    @MockitoBean
    private AssignPositionUseCase assignPositionUseCase;
    @MockitoBean
    private TakeOffDeckUseCase takeOffDeckUseCase;
    @MockitoBean
    private DeclareLoadFinalUseCase declareLoadFinalUseCase;

    @BeforeEach
    void noFlightOrLoadExists() {
        var notFound = new NoSuchElementException("No flight HJ221");
        doThrow(notFound).when(getFlightQuery).getFlight(any());
        doThrow(notFound).when(registerDelayUseCase).registerDelay(any());
        doThrow(notFound).when(changeDelayUseCase).changeDelay(any());
        doThrow(notFound).when(removeDelayUseCase).removeDelay(any());
        doThrow(notFound).when(recordDepartureUseCase).recordDeparture(any());
        doThrow(notFound).when(recordArrivalUseCase).recordArrival(any());
        doThrow(notFound).when(getLoadQuery).getLoad(any());
        doThrow(notFound).when(checkLoadQuery).checkLoad(any());
        doThrow(notFound).when(assignPositionUseCase).assignPosition(any());
        doThrow(notFound).when(takeOffDeckUseCase).takeOffDeck(any());
        doThrow(notFound).when(declareLoadFinalUseCase).declareFinal(any());
    }

    @Test
    void withoutATokenEveryRequestIsUnauthorized() throws Exception {
        mockMvc.perform(get(FLIGHT)).andExpect(status().isUnauthorized());
        mockMvc.perform(registerDelay()).andExpect(status().isUnauthorized());
        mockMvc.perform(assignPosition()).andExpect(status().isUnauthorized());
    }

    @Test
    void everyRoleMayLookAtFlightsAndLoads() throws Exception {
        for (var role : new SimpleGrantedAuthority[]{FLIGHT_PLANNER, LOAD_PLANNER, ADMIN}) {
            mockMvc.perform(get(FLIGHT).with(jwt().authorities(role))).andExpect(status().isNotFound());
            mockMvc.perform(get(LOAD).with(jwt().authorities(role))).andExpect(status().isNotFound());
            mockMvc.perform(get(LOAD + "/checks").with(jwt().authorities(role))).andExpect(status().isNotFound());
            mockMvc.perform(get("/api/flights").with(jwt().authorities(role))).andExpect(status().isOk());
            mockMvc.perform(get("/api/delay-codes").with(jwt().authorities(role))).andExpect(status().isOk());
        }
    }

    @Test
    void onlyAFlightPlannerChangesDelaysAndActualTimes() throws Exception {
        for (var request : flightPlannerRequests()) {
            mockMvc.perform(request.with(jwt().authorities(FLIGHT_PLANNER))).andExpect(status().isNotFound());
        }
        for (var role : new SimpleGrantedAuthority[]{LOAD_PLANNER, ADMIN}) {
            for (var request : flightPlannerRequests()) {
                mockMvc.perform(request.with(jwt().authorities(role))).andExpect(status().isForbidden());
            }
        }
    }

    @Test
    void onlyALoadPlannerChangesTheLoad() throws Exception {
        for (var request : loadPlannerRequests()) {
            mockMvc.perform(request.with(jwt().authorities(LOAD_PLANNER))).andExpect(status().isNotFound());
        }
        for (var role : new SimpleGrantedAuthority[]{FLIGHT_PLANNER, ADMIN}) {
            for (var request : loadPlannerRequests()) {
                mockMvc.perform(request.with(jwt().authorities(role))).andExpect(status().isForbidden());
            }
        }
    }

    @Test
    void aTokenWithoutARoleMayOnlyLook() throws Exception {
        mockMvc.perform(get(FLIGHT).with(jwt())).andExpect(status().isNotFound());
        mockMvc.perform(registerDelay().with(jwt())).andExpect(status().isForbidden());
        mockMvc.perform(assignPosition().with(jwt())).andExpect(status().isForbidden());
    }

    private static MockHttpServletRequestBuilder[] flightPlannerRequests() {
        return new MockHttpServletRequestBuilder[]{
                registerDelay(),
                json(put(FLIGHT + "/delays/7d6f3c1e-2f43-4c7a-9a59-0f1b9c1d2e3f"),
                        "{\"minutes\": 30, \"description\": \"Late inbound aircraft\"}"),
                delete(FLIGHT + "/delays/7d6f3c1e-2f43-4c7a-9a59-0f1b9c1d2e3f"),
                json(post(FLIGHT + "/departure"), "{\"time\": \"2026-11-02T06:40:00Z\"}"),
                json(post(FLIGHT + "/arrival"), "{\"time\": \"2026-11-02T10:05:00Z\"}")};
    }

    private static MockHttpServletRequestBuilder[] loadPlannerRequests() {
        return new MockHttpServletRequestBuilder[]{
                assignPosition(),
                delete(LOAD + "/positions/AKE16689R7"),
                post(LOAD + "/final").param("confirmSoftWarnings", "true")};
    }

    private static MockHttpServletRequestBuilder registerDelay() {
        return json(post(FLIGHT + "/delays"),
                "{\"side\": \"DEPARTURE\", \"minutes\": 20, \"delayCode\": \"93\", \"description\": \"Late inbound aircraft\"}");
    }

    private static MockHttpServletRequestBuilder assignPosition() {
        return json(post(LOAD + "/positions"), "{\"uldCode\": \"AKE16689R7\", \"position\": \"A1\"}");
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
