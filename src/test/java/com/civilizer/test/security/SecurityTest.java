package com.civilizer.test.security;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.springframework.security.authentication.BadCredentialsException;

import com.civilizer.security.UserDetailsService;
import com.civilizer.test.helper.TestUtil;

public class SecurityTest {
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        FileUtils.deleteQuietly(UserDetailsService.getCredentialFile());
        TestUtil.unconfigure();
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.configure();
    }

    @Test
    public void testDefaultCredential() throws Exception {
        FileUtils.deleteQuietly(UserDetailsService.getCredentialFile());
        
        UserDetailsService uds = new UserDetailsService();
        try {
            uds.loadUserByUsername("owner");
        } catch (BadCredentialsException e) {
            fail("default authentication does not work correclty!");
        }
    }
    
    @Test
    public void testCustomCredential() {
        for (int i=0; i<1; ++i) {
            final String username = TestUtil.randomString(TestUtil.getRandom(), 1, 32);
            final String password = TestUtil.randomString(TestUtil.getRandom(), 8, 32);
            try {
                UserDetailsService.saveCustomCredential(username, password);
            } catch (IOException e) {
                fail("failed in saving custom credential!");
                e.printStackTrace();
            }
            
            UserDetailsService uds = new UserDetailsService();
            try {
                uds.loadUserByUsername(username);
            } catch (BadCredentialsException e) {
                fail("custom authentication does not work correclty!");
            }
        }
    }

}
