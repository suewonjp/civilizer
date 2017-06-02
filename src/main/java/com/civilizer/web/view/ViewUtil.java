package com.civilizer.web.view;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import com.civilizer.config.AppOptions;
import com.civilizer.config.Configurator;

public final class ViewUtil {
	
	private static final String MESSAGE_RESOURCE_BASE_NAME = "i18n.MessageResources";
	private static final String HELP_RESOURCE_BASE_NAME = "i18n.HelpResources";

	public static void addMessage(String title, String content, FacesMessage.Severity severity) {
		if (severity == null) {
			severity = FacesMessage.SEVERITY_INFO;
		}
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, content));
	}

	public static void addMessage(String clientId, String title, String content, FacesMessage.Severity severity) {
		if (severity == null) {
			severity = FacesMessage.SEVERITY_INFO;
		}
		FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(severity, title, content));
	}

	public static void addMessage(String objName, Object obj) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(objName, obj.toString()));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T findBean(String beanName) {
	    FacesContext context = FacesContext.getCurrentInstance();
	    if (context != null) {
	        try {
	            return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
            } catch (ELException e) {}
	    }
	    final RequestContext rc = RequestContextHolder.getRequestContext();
	    T r;
        r = (T) rc.getFlowScope().get(beanName);
        if (r == null)
            r = (T) rc.getRequestScope().get(beanName);
        if (r == null)
            r = (T) rc.getViewScope().get(beanName);
        return r;
	}

    public static void setLocale(RequestContext rc) {
        final ServletExternalContext ec = (ServletExternalContext)rc.getExternalContext();
        final HttpServletRequest req = (HttpServletRequest)ec.getNativeRequest();
        Locale locale = RequestContextUtils.getLocale(req); // Retrieve the locale info from the cookie
        // [NOTE] The locale setting via civilizer.locale option precedes the cookie locale.
        locale = Configurator.resolveLocale(locale);
        final UserProfileBean userProfileBean = (UserProfileBean) rc.getFlowScope().get("userProfileBean");
        userProfileBean.setLocale(locale);
    }
	
	public static String getResourceBundleString(String key) {
	    Locale locale;
	    final RequestContext rc = RequestContextHolder.getRequestContext();
	    if (rc == null) {
            locale = new Locale(System.getProperty(AppOptions.CUR_LOCALE));
        }
	    else {
	        final ServletExternalContext ec = (ServletExternalContext)rc.getExternalContext();
	        final HttpServletRequest req = (HttpServletRequest)ec.getNativeRequest();
	        locale = RequestContextUtils.getLocale(req); // Retrieve the locale info from the cookie
	    }
        final ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_RESOURCE_BASE_NAME, locale);
        return bundle.getString(key);
	}

	public static String getHelpString(String key, HttpServletRequest req) {
        final Locale locale = RequestContextUtils.getLocale(req);
	    final ResourceBundle bundle = ResourceBundle.getBundle(HELP_RESOURCE_BASE_NAME, locale);
	    return bundle.getString(key);
	}

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        return principal instanceof UserDetails;
    }

    @SuppressWarnings("unchecked")
    public static <T> void putAttributeToFlowScope(String key, T value) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        MutableAttributeMap<Object> attrMap = requestContext.getFlowScope();
        ArrayList<T> attr = (ArrayList<T>) attrMap.get(key);
        if (attr == null) {
            attrMap.put(key, new ArrayList<T>());
            attr = (ArrayList<T>) attrMap.get(key);
        }
        attr.add(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAttributesFromFlowScope(String key) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        MutableAttributeMap<Object> attrMap = requestContext.getFlowScope();
        return (List<T>) attrMap.get(key);
    }

    public static void removeAttributesFromFlowScope(String key) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        MutableAttributeMap<Object> attrMap = requestContext.getFlowScope();
        attrMap.remove(key);
    }
}
