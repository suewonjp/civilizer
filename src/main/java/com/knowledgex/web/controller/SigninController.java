package com.knowledgex.web.controller;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.RequestContext;

import com.knowledgex.web.view.*;

@Controller
@Component("signinController")
public class SigninController {
	
    private static final String REQUEST_PARAM_AUTH_FAILED = "failed";
//    private static final String USER_IS_AUTHENTICATED = "yes";
//    private static final String USER_IS_NOT_AUTHENTICATED = "no";
    
	@SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(SigninController.class);
    
    public AuthenticationBean newAuthenticationBean() {
        return new AuthenticationBean();
    }
    
    public boolean isAuthenticated() {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	Object principal = auth.getPrincipal();
    	return principal instanceof UserDetails;
    }
    
    public void onEntry(RequestContext rc) {
        final ParameterMap pm = rc.getExternalContext().getRequestParameterMap();
        if (pm.get(REQUEST_PARAM_AUTH_FAILED) != null) {
        	final String msg = ViewUtil.getResourceBundleString("credential_incorrect");
        	
            ViewUtil.addMessage(msg, msg, FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public String handleSignin(AuthenticationBean authBean) throws ServletException, IOException {
        final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        final ServletRequest req = (ServletRequest) context.getRequest();
        final ServletResponse res = (ServletResponse) context.getResponse();
        final RequestDispatcher dispatcher = req.getRequestDispatcher("/j_spring_security_check");

        dispatcher.forward(req, res);

        FacesContext.getCurrentInstance().responseComplete();

        return null;
    }
    
//    @RequestMapping(value = "/authenticated", method = { RequestMethod.GET })
//    @ResponseBody
//    public String isUserAuthenticated() {
//    	if (isAuthenticated()) {
//    		return USER_IS_AUTHENTICATED;
//    	}
//    	else {
//    		return USER_IS_NOT_AUTHENTICATED;
//    	}
//    }

}
