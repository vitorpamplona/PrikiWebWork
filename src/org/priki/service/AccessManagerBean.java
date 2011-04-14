package org.priki.service;

import org.priki.bo.AccessManager;
import org.priki.bo.User;
import org.priki.bo.Wikiword;
import org.priki.utils.StringUtils;

public class AccessManagerBean {
	Session session = new Session(); 
	
	public AccessManagerBean() {
		
	}
	
	public AccessManager getManager() {
		return Prevalence.wiki().getAdmin().getAccessManager();
	}
	
	public String getRole(String user) {
		User userObj = getManager().getUser(user);
		if (getManager().isAdmin(userObj)) {
			return "Admin";
		}
		if (getManager().isEditor(userObj)) {
			return "Editor";
		}
		if (getManager().isReader(userObj)) {
			return "Reader";
		}
		if (getManager().isUser(user)) {
			return "User";
		}
		return "Nothing";
	}
	
	public String getPageType(String keyword) {
		Wikiword w = Prevalence.wiki().getWikiword(keyword);
		if (w == null)
			return "User's Page";
		
		if (w.isToAdmins()) {
			return "Admin's Page";
		}
		if (w.isToEditors()) {
			return "Editor's Page";
		}
		if (w.isToReaders()) {
			return "Reader's Page";
		}
		if (w.isToUsers()) {
			return "User's Page";
		}
		return "Public Page";
	}
	
	public boolean isPublic(String keyword) {
		Wikiword w = Prevalence.wiki().getWikiword(keyword);
		
		if (w == null) return true;
		return w.isPublic();
	}
	
    public boolean isCanPost(String keyword) {
    	String logged = session.getUserLogged();
    	
    	Wikiword w;
    	if (keyword==null)
    		w = null;
    	else
    		w = Prevalence.wiki().getWikiword(keyword);
    	
    	if(StringUtils.isNullAndBlank(logged)) {
    		logged = session.getUser();
    	}
    	
        return Prevalence.wiki().getAdmin().getAccessManager().iCanPost(logged, w);
    }

    public boolean isCanRead(String keyword) {
    	String logged = session.getUserLogged();
    	
    	if(StringUtils.isNullAndBlank(logged)) {
    		logged = session.getUser();
    	}
    	
    	Wikiword w = Prevalence.wiki().getWikiword(keyword);    	

        return Prevalence.wiki().getAdmin().getAccessManager().iCanRead(logged, w);
    }
    
    public boolean isUserCanRead(String keyword, String user) {
    	Wikiword w = Prevalence.wiki().getWikiword(keyword);    	
    	
        return Prevalence.wiki().getAdmin().getAccessManager().iCanRead(user, w);
    }
    
    public boolean isUserCanRead(Wikiword w, String user) {
        return Prevalence.wiki().getAdmin().getAccessManager().iCanRead(user, w);
    }        
}
