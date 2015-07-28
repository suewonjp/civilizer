package com.civilizer.web.view;

import java.io.Serializable;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SuppressWarnings("serial")
public class DataBrokerBean implements Serializable {
    
    private String password = "";
    private boolean exportMode;
    private boolean wrongAuth;
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isExportMode() {
        return exportMode;
    }

    public void setExportMode(boolean exportMode) {
        this.exportMode = exportMode;
    }
    
    public void checkAuth() {
        if (wrongAuth)
            RequestContext.getCurrentInstance().addCallbackParam("wrongAuth", true);
        wrongAuth = false;
    }

    public String onDataExportFlow(FlowEvent event) {
        final String oldStep = event.getOldStep();
        final String newStep = event.getNewStep();
        
        if (oldStep.equals("auth-step") || newStep.equals("auth-step")) {
            wrongAuth = false;
            if (password.isEmpty()) {
                return "auth-step";
            }
            else {
                final String pw = password;
                password = "";
                final Authentication auth =SecurityContextHolder.getContext().getAuthentication();
                final Object principal = auth.getPrincipal();
                if (principal instanceof UserDetails) {
                    final UserDetails ud = (UserDetails) principal;
                    if (new BCryptPasswordEncoder().matches(pw, ud.getPassword()) == false) {
                        wrongAuth = true;
                        return "auth-step";
                    }
                }
                
                return exportMode ? "download-step" : "upload-step";
            }
        }
        else if (oldStep.equals("upload-step")) {
            // Import the uploaded data.
            return "confirm-import-step";
        }
        else if (oldStep.equals("download-step")) {
            return "confirm-export-step";
        }

        return oldStep;
    }

}
