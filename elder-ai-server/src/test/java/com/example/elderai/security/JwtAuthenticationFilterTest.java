package com.example.elderai.security;

import com.example.elderai.service.RedisCacheService;
import com.example.elderai.utils.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPutValidJwtIntoSecurityContext() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        RedisCacheService redisCacheService = mock(RedisCacheService.class);
        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserId("valid-token")).thenReturn(7L);
        when(jwtUtils.getUsername("valid-token")).thenReturn("admin");
        when(jwtUtils.getRole("valid-token")).thenReturn("ADMIN");

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, redisCacheService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/dashboard");
        request.addHeader("Authorization", "Bearer valid-token");
        AtomicBoolean chainReached = new AtomicBoolean(false);

        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> chainReached.set(true));

        assertTrue(chainReached.get());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        AuthenticatedUser principal = (AuthenticatedUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        assertEquals(7L, principal.userId());
        assertEquals("ADMIN", principal.role());
    }

    @Test
    void shouldLeaveInvalidJwtAnonymous() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        RedisCacheService redisCacheService = mock(RedisCacheService.class);
        when(jwtUtils.validateToken("invalid-token")).thenReturn(false);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, redisCacheService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/info");
        request.addHeader("Authorization", "Bearer invalid-token");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
