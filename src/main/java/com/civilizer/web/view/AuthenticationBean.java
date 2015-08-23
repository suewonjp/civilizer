package com.civilizer.web.view;

import java.io.Serializable;

import org.primefaces.context.RequestContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SuppressWarnings("serial")
public final class AuthenticationBean implements Serializable {
    
    private String password = "";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public void validate() {
        final String pw = password;
        password = "";
        
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            final UserDetails ud = (UserDetails) principal;
            if (new BCryptPasswordEncoder().matches(pw, ud.getPassword())) {
                // authentication success...
                RequestContext.getCurrentInstance().addCallbackParam("authenticated", true);
            }
        }
    }

}
