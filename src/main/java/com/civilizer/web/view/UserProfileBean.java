package com.civilizer.web.view;

import java.util.Locale;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class UserProfileBean implements Serializable {
    
    private Locale locale;
    private String userName;
    private String password0;
    private String password1;

    @PostConstruct
    public void init() {
//        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.JAPAN);
        locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            final UserDetails ud = (UserDetails) principal;
            userName = ud.getUsername();
        }
    }

    public Locale getLocale() {
        return locale;
    }
    
    public void setLocale(Locale l) {
        locale = l;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    public String getLanguage() {
        return locale.getLanguage();
    }
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword0() {
        return password0;
    }

    public void setPassword0(String password0) {
        this.password0 = password0;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    @Override
    public String toString() {
        return locale.toString();
    }

}
