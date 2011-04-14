package org.priki.service;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;

public class Session {
	private static final String IMAGEM = nl.captcha.servlet.Constants.SIMPLE_CAPCHA_SESSION_KEY;
	private static final String USER = "user";
	private static final String PASSWORD = "password";
	private static final String ADMIN = "userAdmin";
	
    public Map getSession() {
        return ActionContext.getContext().getSession();
    }
 
    public Object getSessionAttribute(String nameSession) {
        return getSession().get(nameSession);
    }	
	
    public void setSessionAttribute(String nameSession, Object objectSession) {
        getSession().put(nameSession, objectSession);
    }

    public void removeSessionAttribute(String nameSession) {
        getSession().remove(nameSession);
    }
    
    public String getUserLogged() {
    	if (this.getSessionAttribute( USER) == null) return null;
        return this.getSessionAttribute( USER).toString();
    }

    public String getUserPassword() {
    	if (this.getSessionAttribute(PASSWORD) == null) return null;
        return this.getSessionAttribute(PASSWORD).toString();
    }
    
    public String getUser() {
        return ServletActionContext.getRequest().getRemoteAddr();
    }
    
    public boolean isLogged() {
        return this.getSessionAttribute( USER) != null;
    }
    
    public boolean isUserAdmin() {
        return this.getSessionAttribute(ADMIN) != null;
    }
    
    public String getFloodImage() {
    	return ((String)getSession().get(IMAGEM));
    }
    
    public void logoutAdmin() {
    	this.removeSessionAttribute(ADMIN);;
    }
    
    public void loginAdmin(String user) {
    	this.setSessionAttribute(ADMIN, user);
    }
    
    public void loginUser(String user, String password) {
		this.setSessionAttribute(USER, user);
		this.setSessionAttribute(PASSWORD, password);
    }
    
    public void logoutUser() {
		this.removeSessionAttribute( USER);
		this.removeSessionAttribute(PASSWORD);
    }
}
