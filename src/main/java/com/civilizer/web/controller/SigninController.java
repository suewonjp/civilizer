package com.civilizer.web.controller;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.RequestContext;

import com.civilizer.web.view.*;

@Controller
@Component("signinController")
public class SigninController {
	
    private static final String REQUEST_PARAM_AUTH_FAILED = "failed";
//    private static final String USER_IS_AUTHENTICATED = "yes";
//    private static final String USER_IS_NOT_AUTHENTICATED = "no";
    
    public boolean isAuthenticated() {
        return ViewUtil.isAuthenticated();
    }
    
    public void onEntry(RequestContext rc) {
        final ParameterMap pm = rc.getExternalContext().getRequestParameterMap();
        if (pm.get(REQUEST_PARAM_AUTH_FAILED) != null) {
        	final String msg = ViewUtil.getResourceBundleString("credential_incorrect");
        	
            ViewUtil.addMessage(msg, msg, FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public String handleSignin() throws ServletException, IOException {
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
