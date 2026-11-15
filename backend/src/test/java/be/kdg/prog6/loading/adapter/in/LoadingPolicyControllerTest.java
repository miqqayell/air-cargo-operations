package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.config.SecurityConfiguration;
import be.kdg.prog6.loading.domain.CheckKind;
import be.kdg.prog6.loading.domain.LoadingPolicy;
import be.kdg.prog6.loading.port.in.GetLoadingPolicyQuery;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyCommand;
import be.kdg.prog6.loading.port.in.UpdateLoadingPolicyUseCase;
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

import java.math.BigDecimal;
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

@WebMvcTest(controllers = LoadingPolicyController.class)
@Import(SecurityConfiguration.class)
class LoadingPolicyControllerTest {

    private static final String SETTINGS = "/api/policy-settings/loading";
    private static final String SOFT_OFFLOADING = """
            {"windowOpensHoursBeforeStd": 24, "windowClosesMinutesBeforeEtd": 30,
             "maxLateralDifferencePercent": 7.5, "offloadingCheck": "SOFT"}""";
    private static final SimpleGrantedAuthority FLIGHT_PLANNER = new SimpleGrantedAuthority("ROLE_flight-planner");
    private static final SimpleGrantedAuthority LOAD_PLANNER = new SimpleGrantedAuthority("ROLE_load-planner");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_admin");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetLoadingPolicyQuery getLoadingPolicyQuery;
    @MockitoBean
    private UpdateLoadingPolicyUseCase updateLoadingPolicyUseCase;

    @BeforeEach
    void currentPolicy() {
        when(getLoadingPolicyQuery.getLoadingPolicy()).thenReturn(
                new LoadingPolicy(Duration.ofHours(12), Duration.ofMinutes(20), BigDecimal.TEN, CheckKind.HARD));
        when(updateLoadingPolicyUseCase.updateLoadingPolicy(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, UpdateLoadingPolicyCommand.class).loadingPolicy());
    }

    @Test
    void everyRoleReadsTheSettings() throws Exception {
        for (var role : new SimpleGrantedAuthority[]{FLIGHT_PLANNER, LOAD_PLANNER, ADMIN}) {
            mockMvc.perform(get(SETTINGS).with(jwt().authorities(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.windowOpensHoursBeforeStd").value(12))
                    .andExpect(jsonPath("$.windowClosesMinutesBeforeEtd").value(20))
                    .andExpect(jsonPath("$.maxLateralDifferencePercent").value(10))
                    .andExpect(jsonPath("$.offloadingCheck").value("HARD"));
        }
    }

    @Test
    void anAdminChangesTheSettings() throws Exception {
        mockMvc.perform(update(SOFT_OFFLOADING).with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offloadingCheck").value("SOFT"))
                .andExpect(jsonPath("$.maxLateralDifferencePercent").value(7.5));

        verify(updateLoadingPolicyUseCase).updateLoadingPolicy(new UpdateLoadingPolicyCommand(new LoadingPolicy(
                Duration.ofHours(24), Duration.ofMinutes(30), new BigDecimal("7.5"), CheckKind.SOFT)));
    }

    @Test
    void thePlannersMayNotChangeTheSettings() throws Exception {
        mockMvc.perform(update(SOFT_OFFLOADING).with(jwt().authorities(LOAD_PLANNER))).andExpect(status().isForbidden());
        mockMvc.perform(update(SOFT_OFFLOADING).with(jwt().authorities(FLIGHT_PLANNER))).andExpect(status().isForbidden());

        verify(updateLoadingPolicyUseCase, never()).updateLoadingPolicy(any());
    }

    @Test
    void invalidValuesAreRefusedWithAClearMessage() throws Exception {
        mockMvc.perform(update("""
                        {"windowOpensHoursBeforeStd": 12, "windowClosesMinutesBeforeEtd": 20,
                         "maxLateralDifferencePercent": 150, "offloadingCheck": "HARD"}""")
                        .with(jwt().authorities(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The maximum lateral weight difference must be between 0 and 100%"));
        mockMvc.perform(update("""
                        {"windowOpensHoursBeforeStd": 12, "windowClosesMinutesBeforeEtd": 20,
                         "maxLateralDifferencePercent": 10, "offloadingCheck": "MAYBE"}""")
                        .with(jwt().authorities(ADMIN)))
                .andExpect(status().isBadRequest());

        verify(updateLoadingPolicyUseCase, never()).updateLoadingPolicy(any());
    }

    private static MockHttpServletRequestBuilder update(String body) {
        return put(SETTINGS).contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
