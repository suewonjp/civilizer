package com.knowledgex.web.view;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class AuthenticationBean implements Serializable {
	// [TODO] check if the user name is valid (e.g. not already in use, etc)
	// [TODO] check if the password is valid

	private String username;
    
    private String password;
    
    public String getUsername() {
        return username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
     
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
