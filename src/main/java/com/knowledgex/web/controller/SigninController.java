package com.knowledgex.web.controller;

import java.io.IOException;

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

import com.knowledgex.web.view.*;

@Controller
@Component("signinController")
public class SigninController {
	
	private static final Logger logger = LoggerFactory.getLogger(SigninController.class);
    
    public AuthenticationBean newAuthenticationBean() {
        return new AuthenticationBean();
    }
    
    public String handleSignin() throws ServletException, IOException {
        final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        final ServletRequest req = (ServletRequest) context.getRequest();
        final ServletResponse res = (ServletResponse) context.getResponse();
        final RequestDispatcher dispatcher = req.getRequestDispatcher("/j_spring_security_check");

        dispatcher.forward(req, res);

        FacesContext.getCurrentInstance().responseComplete();
        
        logger.info("**** sign in handled");

        return null;
    }
    
//    @RequestMapping(value = "/signin", method = { RequestMethod.GET })
//    public String onSignIn() {
//	    return "signin";
//    }

}
