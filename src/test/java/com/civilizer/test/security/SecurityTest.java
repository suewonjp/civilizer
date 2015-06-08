package com.civilizer.test.security;

import static org.junit.Assert.*;

import org.junit.*;
import org.springframework.security.authentication.BadCredentialsException;

import com.civilizer.security.UserDetailsService;

public class SecurityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDefaultSecurity() {
        UserDetailsService uds = new UserDetailsService();
        try {
            uds.loadUserByUsername("owner");
        } catch (BadCredentialsException e) {
            fail("defualt security does not work correclty!");
        }
    }

}
