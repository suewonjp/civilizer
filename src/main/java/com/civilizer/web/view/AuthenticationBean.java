package com.civilizer.web.view;

import java.io.Serializable;

import org.primefaces.context.RequestContext;

import com.civilizer.security.UserDetailsService;

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
        if (UserDetailsService.authenticatePassword(pw)) {
            // authentication success...
            RequestContext.getCurrentInstance().addCallbackParam("authenticated", true);
        }
    }

}
