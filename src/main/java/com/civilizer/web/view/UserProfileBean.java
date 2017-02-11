package com.civilizer.web.view;

import java.util.*;
import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.civilizer.config.AppOptions;

@SuppressWarnings("serial")
public final class UserProfileBean implements Serializable {
    
    private Locale  locale;
    private String  userName = "";
    private String  password = "";

    private static final String[] themes = {
            "afterdark",
            "afternoon",
            "afterwork",
            "aristo",
            "black-tie",
            "blitzer",
            "bluesky",
            "bootstrap",
            "casablanca",
            "cupertino",
            "cruze",
            "dark-hive",
            "dot-luv",
            "eggplant",
            "excite-bike",
            "flick",
            "glass-x",
            "home",
            "hot-sneaks",
            "humanity",
            "le-frog",
            "midnight",
            "mint-choc",
            "overcast",
            "pepper-grinder",
            "redmond",
            "rocket",
            "sam",
            "smoothness",
            "south-street",
            "start",
            "sunny",
            "swanky-purse",
            "trontastic",
            "ui-darkness",
            "ui-lightness",
            "vader",
    };

    @PostConstruct
    public void init() {
        retrieveCurAuth();
    }

    public void retrieveCurAuth() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            final UserDetails ud = (UserDetails) principal;
            userName = ud.getUsername();
        }
    }

    public Locale getLocale() {
        if (locale == null) {
            setLocale(System.getProperty(AppOptions.LOCALE));
        }
        return locale;
    }

    public void setLocale(Locale l) {
        locale = l;
        System.setProperty(AppOptions.LOCALE, l.getLanguage());
    }

    private void setLocale(String localeName) {
        localeName = localeName.toLowerCase(); // Any locale name is case insensitive
        if (localeName.equals("en")) {
            setLocale(Locale.ENGLISH);
        }
        else if (localeName.equals("ja") || localeName.equals("jp")) {
            setLocale(Locale.JAPANESE);
        }
//        else if (locale.equals("ko") || locale.equals("kr")) {
//            setLocale(Locale.KOREAN);
//        }
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String[] getThemes() {
        return themes;
    }
    
    @Override
    public String toString() {
        return locale.toString();
    }

}
