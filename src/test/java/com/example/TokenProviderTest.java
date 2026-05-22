package com.example;

import com.example.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void contextLoads() {
        assertNotNull(jwtTokenProvider);
    }

    @Test
    void shouldCreateAndValidateAccessToken() {
        String token = jwtTokenProvider.createAccessToken("testuser", "USER");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateAccessToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
        assertEquals("USER", jwtTokenProvider.getRoleFromToken(token));
    }
}
