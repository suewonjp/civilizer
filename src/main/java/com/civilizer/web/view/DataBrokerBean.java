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
    
    private String curStep = "";
    private String password = "";
    private boolean exportMode;
    private boolean authFailed;
    
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
    
    public void checkNext() {
        if (curStep.equals("auth-step")) {
            if (authFailed)
                RequestContext.getCurrentInstance().addCallbackParam("authFailed", true);
            authFailed = false;
        }
        else if (curStep.equals("predownload-step")) {
            RequestContext.getCurrentInstance().addCallbackParam("exportReady", true);
        }
    }
    
//    private void importData() {
//        
//    }

    private void exportData() throws Exception {
        
    }
    
    public void packExportData() {
        if (curStep.equals("predownload-step")) {
            boolean ok = true;
            try {
                exportData();
            } catch (Exception e) {
                e.printStackTrace();
                ok = false;
            }
            RequestContext.getCurrentInstance().addCallbackParam("exportReady", ok);
        }
    }

    public String onDataExportFlow(FlowEvent event) {
        final String oldStep = event.getOldStep();
        final String newStep = event.getNewStep();
        
        if (oldStep.equals("auth-step") || newStep.equals("auth-step")) {
            // Authentication step.
            authFailed = false;
            if (password.isEmpty()) {
                return (curStep = "auth-step");
            }
            else { // the password has been provided
                final String pw = password;
                password = "";
                
                final Authentication auth =SecurityContextHolder.getContext().getAuthentication();
                final Object principal = auth.getPrincipal();
                if (principal instanceof UserDetails) {
                    final UserDetails ud = (UserDetails) principal;
                    if (new BCryptPasswordEncoder().matches(pw, ud.getPassword()) == false) {
                        // authentication failed...
                        authFailed = true;
                        return (curStep = "auth-step");
                    }
                }
                
                return (curStep = exportMode ? "predownload-step" : "upload-step");
            }
        }
        else if (oldStep.equals("upload-step")) {
            // Import the uploaded data.
            return (curStep = "confirm-import-step");
        }
        else if (oldStep.equals("predownload-step")) {
            return (curStep = "download-step");
        }
        else if (oldStep.equals("download-step")) {
            return (curStep = "confirm-export-step");
        }

        return oldStep;
    }

}
