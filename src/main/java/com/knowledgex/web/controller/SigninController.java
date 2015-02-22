package com.knowledgex.web.controller;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.knowledgex.web.view.*;

@Controller
@Component("signinController")
public class SigninController {
    
    public AuthenticationBean newAuthenticationBean() {
        return new AuthenticationBean();
    }

}
