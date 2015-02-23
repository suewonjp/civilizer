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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.RequestContext;

import com.knowledgex.web.view.*;

@Controller
@Component("signinController")
public class SigninController {
	
    private static final String REQUEST_PARAM_AUTH_FAILED = "failed";
    
	@SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(SigninController.class);
    
    public AuthenticationBean newAuthenticationBean() {
        return new AuthenticationBean();
    }
    
    public void onEntry(RequestContext rc) {
        final ParameterMap pm = rc.getExternalContext().getRequestParameterMap();
        if (pm.get(REQUEST_PARAM_AUTH_FAILED) != null) {
            ViewUtil.addMessage("Username or password not valid", "Username or password not valid", FacesMessage.SEVERITY_ERROR);
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

}
