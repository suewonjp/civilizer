package com.civilizer.security;

import java.util.*;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsService
    implements org.springframework.security.core.userdetails.UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        if (username.equals("owner")) {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new User(username, "$2a$10$w.Jjtx0mrjH4E.DxQEmBZu.D1oCBKy26utS8KCOSn0fmq1xs2GXiK", authorities);
        }
        throw new BadCredentialsException("Bad Credentials");
    }
    
}
