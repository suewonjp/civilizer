package com.civilizer.security;

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.civilizer.config.AppOptions;
import com.civilizer.utils.FsUtil;

public final class UserDetailsService
    implements org.springframework.security.core.userdetails.UserDetailsService {
    
    public static final int ENCRYPTION_STRENTH = 11;
    public static final String CREDENTIAL_FILE = "cvz.pc";
    
    private static UserDetails createUserDetails(String username, String passwordCode) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username, passwordCode, authorities);
    }

    private static UserDetails getDefaultCredential(String username) {
        if (username.equals("owner")) {
            return createUserDetails(username, "$2a$10$w.Jjtx0mrjH4E.DxQEmBZu.D1oCBKy26utS8KCOSn0fmq1xs2GXiK");
        }
        return null;
    }
    
    private static UserDetails getCustomCredential(String username, String usernameCode, String passwordCode) {
        if (new BCryptPasswordEncoder().matches(username, usernameCode)) {
            return createUserDetails(username, passwordCode);
        }
        return null;
    }
    
    public static File getCredentialFile() {
        final String homePath = System.getProperty(AppOptions.PRIVATE_HOME_PATH); 
        final String credFilePath = FsUtil.getAbsolutePath(CREDENTIAL_FILE, homePath);
        return new File(credFilePath);
    }
    
    public static void saveCustomCredential(String username, String password) throws IOException {
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(ENCRYPTION_STRENTH);
        final List<String> lines = new ArrayList<>();
        lines.add(encoder.encode(username));
        lines.add(encoder.encode(password));
        FileUtils.writeLines(UserDetailsService.getCredentialFile(), lines, null);
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        final Logger logger = LoggerFactory.getLogger(UserDetailsService.class);
        final File credentialFile = getCredentialFile();
        UserDetails output = null;
        boolean customCredential = credentialFile.isFile();
        
        if (customCredential) {
            try {
                final String content = FileUtils.readFileToString(credentialFile);
                final String[] tmp = content.split("\n");
                logger.info("processing custom credential...");
                output = getCustomCredential(username, tmp[0], tmp[1]);
            } catch (IOException e) {
                logger.error("Error in reading the file {}", credentialFile.getAbsoluteFile());
                e.printStackTrace();
                logger.error("Switching to default credential...");
                customCredential = false;
            }
        }
        
        if (customCredential == false) {
            logger.info("processing default credential...");
            output = getDefaultCredential(username);
        }
        
        if (output != null) {
            return output;
        }
        
        logger.info("authentication for {} failed!", username);
        throw new BadCredentialsException("Bad Credentials");
    }
    
}
