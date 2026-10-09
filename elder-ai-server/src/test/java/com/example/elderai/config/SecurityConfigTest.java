package com.example.elderai.config;

import com.example.elderai.controller.AuthController;
import com.example.elderai.security.ClientIpResolver;
import com.example.elderai.security.RateLimitService;
import com.example.elderai.service.UserService;
import com.example.elderai.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        properties = {
                "app.cors.allowed-origins=http://localhost:5173",
                "app.security.rate-limit.enabled=false"
        }
)
@Import({SecurityConfig.class, CorsConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private ClientIpResolver clientIpResolver;

    @Test
    void protectedEndpointShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void adminEndpointShouldReturn403ForElderRole() throws Exception {
        when(jwtUtils.validateToken("elder-token")).thenReturn(true);
        when(jwtUtils.getUserId("elder-token")).thenReturn(9L);
        when(jwtUtils.getUsername("elder-token")).thenReturn("elder");
        when(jwtUtils.getRole("elder-token")).thenReturn("ELDER");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer elder-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void loginEndpointShouldRemainPublic() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void missingPublicResourceShouldReturn404InsteadOf500() throws Exception {
        mockMvc.perform(get("/api/news/not-existing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
