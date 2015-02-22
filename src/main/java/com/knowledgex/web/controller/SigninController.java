package com.knowledgex.web.controller;

import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.knowledgex.web.view.*;

@Controller
//@Component("signinController")
@ManagedBean(name="signinController")
@RequestScoped
public class SigninController {
	
	private static final Logger logger = LoggerFactory.getLogger(SigninController.class);
    
    public AuthenticationBean newAuthenticationBean() {
        return new AuthenticationBean();
    }
    
    public String handleSignin() throws ServletException, IOException {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

        RequestDispatcher dispatcher = ((ServletRequest) context.getRequest())
                .getRequestDispatcher("/j_spring_security_check");

        dispatcher.forward((ServletRequest) context.getRequest(),
                (ServletResponse) context.getResponse());

        FacesContext.getCurrentInstance().responseComplete();
        
        logger.info("**** sign in handled");

        return null;
    }
    
    @RequestMapping(value = "/signin", method = { RequestMethod.GET })
    public String onSignIn() {
	    return "signin";
    }

}
