package be.kdg.prog6.simulator;

import be.kdg.prog6.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SimulatorController.class)
@Import(SecurityConfiguration.class)
class SimulatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedPublisher feedPublisher;

    @Test
    void onlyAnAdminRunsTheSimulator() throws Exception {
        mockMvc.perform(simulateSchedules().with(jwt().authorities(new SimpleGrantedAuthority("ROLE_admin"))))
                .andExpect(status().isAccepted());
        mockMvc.perform(simulateSchedules().with(jwt().authorities(new SimpleGrantedAuthority("ROLE_flight-planner"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(simulateSchedules().with(jwt().authorities(new SimpleGrantedAuthority("ROLE_load-planner"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(simulateSchedules()).andExpect(status().isUnauthorized());
    }

    private static MockHttpServletRequestBuilder simulateSchedules() {
        return post("/api/simulator/schedules").contentType(MediaType.APPLICATION_JSON).content("[]");
    }
}
