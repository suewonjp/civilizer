package com.civilizer.web.view;

import java.io.Serializable;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class LocaleBean implements Serializable {
	
	@SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(LocaleBean.class);

    private Locale locale;

    @PostConstruct
    public void init() {
//        FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.JAPAN);
        locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
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
    
    @Override
    public String toString() {
    	return locale.toString();
    }

}
