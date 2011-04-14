/*
 * Priki - Prevalent Wiki
 * Copyright (c) 2005 Priki 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 *
 * @author Vitor Fernando Pamplona - vitor@babaxp.org
 *
 */
package org.priki.actions;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.priki.bo.I18N;
import org.priki.bo.Plugin;
import org.priki.bo.Wikiword;
import org.priki.service.DateUtil;
import org.priki.service.Prevalence;
import org.priki.service.Session;
import org.priki.utils.SortedBag;
import org.priki.utils.StringUtils;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.util.OgnlValueStack;

/**
 * Holds the control of Captcha (the flood security)
 * Message errors
 * User Context
 * Layout Changes
 *
 * @author <a href="mailto:vitor@babaxp.org">Vitor Fernando Pamplona</a>
 *
 * @since 01/11/2005
 * @version $Id: $
 */
public class SuperAction extends ActionSupport {
    
	private static int LEFT_MENU=1;
	private static int RIGHT_MENU=2;
	private static int BEFORE_CONTENT=3;
	private static int AFTER_CONTENT=4;
	private static int BEFORE_COMMENTS=5;
	private static int AFTER_COMMENTS=6;
	private static int INSIDE_HEAD=7;
	
    private String floodValidation;
    private String messageError;
    private String redirectTo;
    
	Session session = new Session(); 
	
	private List<Plugin> plugins;
	
	private List<Plugin> plugins() {
		if (plugins == null)
			plugins = Prevalence.wiki().getAdmin().getPlugins();
		return plugins;
	}
	
	/** Problems with urls when using these chars */
	protected boolean isAValidKeyword(String keyword) {
		return (!keyword.contains("/") && !keyword.contains("+") && !keyword.contains("?"));
	}
	
	private List<Plugin> filterPlugins(int position) {
		List<Plugin> ret = new ArrayList<Plugin>();
		for (Plugin p : plugins()) {
			if (p.getPosition() == position)
				ret.add(p);
		}
		Collections.sort(ret);
		return ret;
	}
	
	public List<Plugin> getPluginsLeftMenu() { 
		return filterPlugins(LEFT_MENU);
	}
	
	public List<Plugin> getPluginsRightMenu() { 
		return filterPlugins(RIGHT_MENU);
	}
	
	public List<Plugin> getPluginsBeforeContent() { 
		return filterPlugins(BEFORE_CONTENT);
	}
	
	public List<Plugin> getPluginsAfterContent() { 
		return filterPlugins(AFTER_CONTENT);
	}
	
	public List<Plugin> getPluginsBeforeComments() { 
		return filterPlugins(BEFORE_COMMENTS);
	}
	
	public List<Plugin> getPluginsAfterComments() { 
		return filterPlugins(AFTER_COMMENTS);
	}
	
	public List<Plugin> getPluginsInsideHead() { 
		return filterPlugins(INSIDE_HEAD);
	}
    
    public void setFloodValidation(String floodValidation) {
        this.floodValidation = floodValidation;
    }
    
    public boolean isFloodOK() {
        if (session.isLogged()) return true;
        return floodValidation.equalsIgnoreCase(session.getFloodImage());
    }
    
    public String dateRFCFormat(Date date){
        try{
            return DateUtil.dateRFCFormat(date);
        }catch(Exception e){
            return "";
        }
    }    
    
	public String getDefaultI18n() {
		return Prevalence.wiki().getAdmin().getDefaultI18n();
	}
    
    public String removeHTML(String text) {
        return text.replaceAll("<.*?>", ""); 
    }

    public String getErrorMessage() {
        return messageError;
    }
    
    public void setErrorMessage(String str) {
        messageError = str;
    }
    
    public String getUserIP() {
        return ServletActionContext.getRequest().getRemoteAddr();
    }
    
    public boolean isLogged() {
    	return session.isLogged();
    }
    
    public boolean isUserAdmin() {
        return session.isUserAdmin();
    }
    
    public String getUserLogged() {
    	return session.getUserLogged();
    }

    public SortedBag<Wikiword> getGlobalTags() { 
    	return Prevalence.wiki().getGlobalTags(); 
    }
    
    public String getUserNameLogged() {
    	return Prevalence.wiki().getAdmin().getAccessManager().getUser(session.getUserLogged()).getCompleteName();
    }
    
    public boolean isReadonlyWiki() {
        return Prevalence.wiki().getAdmin().getAccessManager().isReadonly();
    }

    public boolean isLastChangesAsItems() {
        return Prevalence.wiki().getAdmin().isLastChangesInItems();
    }
    
    public Format getDf() {
        return DateFormat.getDateInstance();
    }

    public String getPath() {
        return Prevalence.wiki().getAdmin().getBasePath();
    }
    
    public String getSiteName() {
        return Prevalence.wiki().getAdmin().getSiteName();
    }
    
    public String getSlogan() {
        return Prevalence.wiki().getAdmin().getSlogan();
    }
    
    public boolean checkString(String str, String message) {
        if (StringUtils.isNullAndBlank(str)) {
        	setErrorMessage("priki.cantBeNull",message);
            return false;
        }
        return true;
    }

    public String escapeXml(String text)  { return StringEscapeUtils.escapeXml(text); }
    public String escapeHTML(String text) { return StringEscapeUtils.escapeHtml(text); }
    
    public String getURL(String url) { 
    	try {
			return new URL(url).toURI().toASCIIString();
		} catch (MalformedURLException e) {
			return url;
		} catch (URISyntaxException e) {
			return url;
		} 
    }
    
    public boolean checkInteger(Integer integ, String message) {
        if (integ == null) {
        	setErrorMessage("priki.cantBeNull",message);
            return false;
        }
        return true;
    }
    
    public void setErrorMessage(String i18nName, String param1) {
    	List<String> lista = new ArrayList<String>();
    	lista.add(param1);
    	
    	setErrorMessage(getText(i18nName,lista));
    }
    
    public void setErrorMessage(String i18nName, String param1, String param2) {
    	List<String> lista = new ArrayList<String>();
    	lista.add(param1);
    	lista.add(param2);
    	
    	setErrorMessage(getText(i18nName,lista));
    }

	public String getRedirectTo() {
		return redirectTo;
	}

	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
	}    
	
	
	public List<String> getAllI18NKeys() {
		Enumeration<String> allKeys = getTexts("org.priki.actions.package").getKeys();
		ArrayList<String> ret = new ArrayList<String>();
		while (allKeys.hasMoreElements()) {
			ret.add(allKeys.nextElement());
		}
		Collections.sort(ret);
		return ret;
	}

	public String getI18NOverride(String key, List args) {
		I18N i18n = Prevalence.wiki().getAdmin().getI18NOverride(getLocale().getLanguage()+"_"+getLocale().getCountry(), key);
		if (i18n != null) {
			return MessageFormat.format(i18n.getText(), args);
		}
		return null;
	}
	
	@Override
	public String getText(String textName, List args) {
		String text = getI18NOverride(textName, args);
		if (text != null) return text;
		return super.getText(textName, args);
	}

	@Override
	public String getText(String key, String defaultValue, List args,
			OgnlValueStack stack) {
		String text = getI18NOverride(key, args);
		if (text != null) return text;
		return super.getText(key, defaultValue, args, stack);
	}

	@Override
	public String getText(String textName, String defaultValue, List args) {
		String text = getI18NOverride(textName, args);
		if (text != null) return text;
		return super.getText(textName, defaultValue, args);
	}

	@Override
	public String getText(String textName, String defaultValue) {
		String text = getI18NOverride(textName, new ArrayList());
		if (text != null) return text;
		return super.getText(textName, defaultValue);
	}

	@Override
	public String getText(String textName) {
		String text = getI18NOverride(textName, new ArrayList());
		if (text != null) return text;
		return super.getText(textName);
	}
	
	
}
