package com.civilizer.test.security

import spock.lang.*;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.apache.commons.io.FileUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;

import com.civilizer.security.UserDetailsService;
import com.civilizer.test.helper.TestUtil;

@Subject(UserDetailsService)
class SecuritySpec extends spock.lang.Specification {
    
    def cleanupSpec() {
        FileUtils.deleteQuietly(UserDetailsService.getCredentialFile());
        TestUtil.unconfigure();
    }
    
    def setup() {
        TestUtil.configure();
    }
    
    def "The default credential"() {
        setup: "Revoke any of existing credentials"
            FileUtils.deleteQuietly(UserDetailsService.getCredentialFile());
        and: "A service object to manage user credentials"
            UserDetailsService uds = new UserDetailsService();
            
        when: "Load the default credential, which is (username:owner, pw:owner)"
            uds.loadUserByUsername("owner");
        then:
            notThrown BadCredentialsException
    }
    
    def "Invalid credentials"() {
        when: "Invalid credentials were given"
            UserDetailsService.saveCustomCredential(usernm, passwd, null);
        then:
            thrown InvalidParameterException
            
        where: "Table of possible invalid credentials"
            usernm      | passwd
            null        | null  
            null        | ""    
            ""          | null  
            ""          | ""    
            "username"  | ""    
            ""          | "password"
    }
    
    def checkCustomCredential() {
        given: "Random username and password"
            final String un = TestUtil.randomString(TestUtil.getRandom(), 1, 32);
            final String pw = TestUtil.randomString(TestUtil.getRandom(), 8, 32);
        
        when: "Save them"
            UserDetailsService.saveCustomCredential(un, pw, null);
        then:
            notThrown exception
            
        when: "Load the stored credential"
            final UserDetailsService uds = new UserDetailsService();
            UserDetails ud = uds.loadUserByUsername(un);
        then:
            notThrown BadCredentialsException
        and: "The credential matches?"
            un == ud.getUsername()
            UserDetailsService.encodingMatches(pw, ud.getPassword())
            
        where: 
            exception << [ IOException, InvalidParameterException ]
    }
    
    def "Custom credentials"() {
        2.times {
            checkCustomCredential();
        }
    }

}
