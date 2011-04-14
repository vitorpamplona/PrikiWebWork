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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.priki.bo.AccessManager;
import org.priki.bo.Admin;
import org.priki.bo.I18N;
import org.priki.bo.Plugin;
import org.priki.bo.User;
import org.priki.bo.Wikiword;
import org.priki.prevalence.AddEscortWikiwordTransaction;
import org.priki.prevalence.AddI18NTransaction;
import org.priki.prevalence.AddPluginTransaction;
import org.priki.prevalence.AdminTransaction;
import org.priki.prevalence.ChangeUserRoleTransaction;
import org.priki.prevalence.EditI18NTransaction;
import org.priki.prevalence.EditPluginTransaction;
import org.priki.prevalence.FixWikiRefersTransaction;
import org.priki.prevalence.GoodLinkTransaction;
import org.priki.prevalence.GoodUserTransaction;
import org.priki.prevalence.RemoveCommentTransaction;
import org.priki.prevalence.RemoveEscortWikiwordTransaction;
import org.priki.prevalence.RemoveI18NTransaction;
import org.priki.prevalence.RemovePluginTransaction;
import org.priki.prevalence.RollbackTransaction;
import org.priki.service.Prevalence;
import org.priki.utils.StringUtils;

public class AdminAction extends SuperAction {

    private String siteName;
    private String slogan;    
    private String adminBasePath;
    private String defaultI18n;  
    private Integer lastChangesCount;
    private String lastChangesInItems;
    private Integer signUpAs;
    private Integer defaultVisibility;
    private String defaultCaseSensitive;
    private String lastChangesOnlyNewPages;
    private String readonly;
    private String acceptAnonymousReader;
    private String acceptAnonymousEditor;

	private String smtpHost;
	private String smtpUser;
	private String smtpPassword;
	private String smtpPort;
	private String fromMail;
	private String fromName;
    
    private String rollbackDate;
    private String userIP;
    private String link;
    private String user;
    
    private String userIsReader;
    private String userIsEditor;
    private String userIsAdmin;
    private String userCompleteName;
    private String userEmail;
    private String userEscortWiki;
    private List<String> userEscortingPages;
   
	private String pluginName;
    private String pluginNewName;
    private Integer pluginPosition;
    private Integer pluginOrder;
    private String pluginHTML;
    
    private String i18nLanguage;
    private String i18nNewLanguage;
    private String i18nKey;
    private String i18nNewKey;
    private String i18nText;
    
    private List<String> badUsers;
    private List<String> badLinks;
    private List<User> users;
    private List<I18N> i18ns;
    
    
    private String keyword;
    private Integer commentIndex;
    
    public List<String> getBadUsers() { return this.badUsers; }
    public List<String> getBadLinks() { return this.badLinks; }
    public List<I18N> getI18ns() { return this.i18ns; }
    
    // --------------------------------
    
    private Admin getAdmin() {
        return Prevalence.wiki().getAdmin();
    }

    public String showConfig() {
        
        siteName  = getAdmin().getSiteName();
        adminBasePath  = getAdmin().getBasePath();
        slogan = getAdmin().getSlogan();
        lastChangesInItems = getAdmin().isLastChangesInItems() ? getText("priki.yes") : getText("priki.no");
        lastChangesOnlyNewPages = getAdmin().isLastChangesOnlyNewPages() ? getText("priki.yes") : getText("priki.no");
        readonly  = getAdmin().getAccessManager().isReadonly() ? getText("priki.yes") : getText("priki.no");
        acceptAnonymousEditor = getAdmin().getAccessManager().isAcceptAnonymousEditor() ? getText("priki.yes") : getText("priki.no");
        acceptAnonymousReader = getAdmin().getAccessManager().isAcceptAnonymousReader() ? getText("priki.yes") : getText("priki.no");
        defaultCaseSensitive = getAdmin().getAccessManager().isDefaultCaseSensitive() ? getText("priki.yes") : getText("priki.no");
        defaultVisibility = getAdmin().getAccessManager().getDefaultVisibility().ordinal();        
        lastChangesCount = getAdmin().getLastChangesCount();
        badUsers  = Prevalence.wiki().getPolice().getBadUsers();
        badLinks  = Prevalence.wiki().getPolice().getBadLinks();
        users    = getAdmin().getAccessManager().getAllUsers();
        signUpAs = getAdmin().getAccessManager().getSignUp().ordinal();
        defaultI18n = getAdmin().getDefaultI18n();
        
    	smtpHost = getAdmin().getMailConfiguration().getSmtpHost();
    	smtpUser = getAdmin().getMailConfiguration().getSmtpUser();
    	smtpPassword = getAdmin().getMailConfiguration().getSmtpPassword();
    	smtpPort = getAdmin().getMailConfiguration().getSmtpPort();
    	fromMail = getAdmin().getMailConfiguration().getFromMail();
    	fromName = getAdmin().getMailConfiguration().getFromName();
        
    	i18ns = getAdmin().getI18NOverrides();
    	
        return SUCCESS;
    }
    
	public String snapshot() {
		try {
			Prevalence.getInstance().snapshot();
		} catch (IOException e) {
			setErrorMessage(getText("priki.snapshotIOError"));
			return ERROR;
		}
		return SUCCESS;
	}
	
    public String postConfig() {
        if (!checkString(siteName, getText("priki.admin.siteName"))
        ||  !checkString(adminBasePath, getText("priki.admin.basePath"))
        ||  !checkInteger(this.signUpAs, getText("priki.admin.signUpAs"))) {
            return ERROR;
        }
        
        boolean bLastChangesAsItems = "lastChangesInItems".equals(lastChangesInItems);
        boolean bReadonly = "readonly".equals(readonly);
        boolean bAcceptEditor = "acceptAnonymousEditor".equals(acceptAnonymousEditor);
        boolean bAcceptReader = "acceptAnonymousReader".equals(acceptAnonymousReader);
        boolean bDefaultCaseSensitive = "defaultCaseSensitive".equals(defaultCaseSensitive);
        boolean bLastChangesOnlyNewPages = "lastChangesOnlyNewPages".equals(lastChangesOnlyNewPages);
               
        if (adminBasePath.endsWith("/"))
        	adminBasePath = adminBasePath.substring(0, adminBasePath.length()-1);
        
        AccessManager.SignUp signUpAs = AccessManager.SignUp.AsEditor;
        signUpAs = AccessManager.SignUp.values()[this.signUpAs.intValue()];
        
        Wikiword.Visibility visibility = Wikiword.DEFAULT_VISIBILITY;
        visibility = Wikiword.Visibility.values()[this.defaultVisibility.intValue()];
        
        AdminTransaction trans = new AdminTransaction(siteName, slogan, adminBasePath, defaultI18n, 
        											lastChangesCount, bLastChangesAsItems, bLastChangesOnlyNewPages,
        											bReadonly, bAcceptEditor, bAcceptReader, signUpAs, 
        											visibility, bDefaultCaseSensitive,
        									    	smtpHost, smtpUser, smtpPassword, smtpPort,	fromMail,fromName);
        Prevalence.getInstance().execute(trans);
        
        return SUCCESS;
    }
    
    public String manageUser() {
        siteName  = getAdmin().getSiteName();
        adminBasePath  = getAdmin().getBasePath();
        slogan = getAdmin().getSlogan();    
    	
    	User reggistered = Prevalence.wiki().getAdmin().getAccessManager().getUser(user);
    	
    	if (reggistered == null) {
    		setErrorMessage(getText("priki.userDoesNotExists", user));
    		return ERROR;
    	}
    	
    	userCompleteName = reggistered.getCompleteName();
    	userEmail = reggistered.getEmail();
		userIsAdmin = "no";
		userIsReader = "no";
		userIsEditor = "no";
		userEscortWiki = "no";
		
		userEscortingPages = new ArrayList<String>();
		for (String w : Prevalence.wiki().getWikiwords()) {
			if (Prevalence.wiki().getWikiword(w).getEscortWikiWord().contains(reggistered)) {
				userEscortingPages.add(w);
			}
		}	
		
    	if (Prevalence.wiki().getAdmin().getAccessManager().isAdmin(reggistered)) {
    		userIsAdmin = "yes";
    	}
    	
    	if (Prevalence.wiki().getAdmin().getAccessManager().isEditor(reggistered)) {
    		userIsEditor = "yes";
    	}
    	
    	if (Prevalence.wiki().getAdmin().getAccessManager().isReader(reggistered)) {
    		userIsReader = "yes";
    	}
    	
    	if (reggistered.isEscortWiki()) {
    		userEscortWiki = "yes";
    	}    	

    	return SUCCESS;
    }
    
    public String changeUserRole() {
    	User reggistered = Prevalence.wiki().getAdmin().getAccessManager().getUser(user);
    	
    	if (reggistered == null) {
    		setErrorMessage("priki.userDoesNotExists", user);
    		return ERROR;
    	}

    	boolean isReader = "true".equals(userIsReader);
    	boolean isEditor = "true".equals(userIsEditor);
    	boolean isAdmin  = "true".equals(userIsAdmin);
    	boolean escortWiki  = "true".equals(userEscortWiki);
    	
    	ChangeUserRoleTransaction transaction = new ChangeUserRoleTransaction(user, userEmail, userCompleteName, isReader, isEditor, isAdmin, escortWiki);
    	Prevalence.getInstance().execute(transaction);
    	
    	return SUCCESS;
    }
    
    public String escortWikiword() {
    	keyword = StringEscapeUtils.unescapeHtml(keyword.trim());
    	User u = Prevalence.wiki().getAdmin().getAccessManager().getUser(user);
    	
        if (u == null) {
        	setErrorMessage("priki.userNotFound");
            return ERROR;
        }
               
        AddEscortWikiwordTransaction trans = new AddEscortWikiwordTransaction(u.getIdentifier(), keyword);
        Prevalence.getInstance().execute(trans);
        
        try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			setErrorMessage("priki.invalidConversionUTF-8", keyword);
			return ERROR;
		}        
        
        return SUCCESS;
    }
    
    public String unescortWikiword() {
    	keyword = StringEscapeUtils.unescapeHtml(keyword.trim());
    	User u = Prevalence.wiki().getAdmin().getAccessManager().getUser(user);
    	
        if (u == null) {
        	setErrorMessage("priki.userNotFound");
            return ERROR;
        }
               
        RemoveEscortWikiwordTransaction trans = new RemoveEscortWikiwordTransaction(u.getIdentifier(), keyword);
        Prevalence.getInstance().execute(trans);
        
        try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			setErrorMessage("priki.invalidConversionUTF-8", keyword);
			return ERROR;
		}        
        
        return SUCCESS;
    }        
    
    public String removeComment() {
        
    	if (!checkString(keyword, getText("priki.keyword"))
    	||  !checkInteger(commentIndex, getText("priki.commentIndex"))) {
    		return ERROR;
    	}
    	
        RemoveCommentTransaction trans = new RemoveCommentTransaction(keyword, commentIndex);
        Prevalence.getInstance().execute(trans);
        
        try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			setErrorMessage("priki.invalidConversionUTF8", keyword);
			return ERROR;
		}
        
        return SUCCESS;
    }    
    
    public String rollback() {
        
        Date date = null;
        try {
            date = DateFormat.getDateInstance().parse(rollbackDate);
        } catch (ParseException e) {
            e.printStackTrace();
            setErrorMessage(getText("priki.invalidDateFormat"));
            return ERROR;
        }
        
        if (!checkString(userIP, getText("priki.userProfile.user"))) {
            return ERROR;
        }
        
        RollbackTransaction trans = new RollbackTransaction(date, userIP);
        Prevalence.getInstance().execute(trans);
        
        return SUCCESS;
    }
    
    public String goodUser() {
        if (!checkString(userIP, getText("priki.userProfile.user"))) {
            return ERROR;
        }

        GoodUserTransaction trans = new GoodUserTransaction(userIP);
        Prevalence.getInstance().execute(trans);
        
        return SUCCESS;
    }
    
    public String goodLink() {
        if (!checkString(link, getText("priki.link"))) {
            return ERROR;
        }
        
        GoodLinkTransaction trans = new GoodLinkTransaction(link);
        Prevalence.getInstance().execute(trans);
        
        return SUCCESS;
    }
    
    public String removePlugin() {
    	if (!checkString(pluginName, getText("priki.admin.plugin.name"))) {
            return ERROR;
        }
        
    	RemovePluginTransaction trans = new RemovePluginTransaction(pluginName);
    	Prevalence.getInstance().execute(trans);
    	 
    	return SUCCESS;
    }
    
    public String showPlugin() {  	
        siteName  = getAdmin().getSiteName();
        adminBasePath  = getAdmin().getBasePath();
        slogan = getAdmin().getSlogan();    
    	
    	Plugin plg = Prevalence.wiki().getAdmin().getPlugin(pluginName);
    	
    	if (plg == null) {
    		pluginHTML = "";
    		pluginNewName = "";
    		pluginName = "";
    		pluginPosition=0;
    		pluginOrder= 0;
    	} else {
    		pluginHTML = plg.getHtml();
    		pluginNewName = plg.getName();
    		pluginName = plg.getName();
    		pluginPosition= plg.getPosition();		
    		pluginOrder = plg.getOrder();
    	}
    	
	 
    	return SUCCESS;
    }
       
    public String updatePlugin() {
    	if (!checkString(pluginNewName, getText("priki.admin.plugin.name"))) {
            return ERROR;
        }
    	
    	if (pluginName == null || pluginName.trim().equals("")) {
        	AddPluginTransaction trans = new AddPluginTransaction(pluginNewName, pluginPosition, pluginHTML, pluginOrder);
        	Prevalence.getInstance().execute(trans);
    	}else{
    		EditPluginTransaction trans = new EditPluginTransaction(pluginName, pluginNewName, pluginPosition, pluginHTML, pluginOrder);
    		Prevalence.getInstance().execute(trans);
    	}
    	 
    	return SUCCESS;
    }
    
    public String removeI18N() {
    	if (!checkString(i18nLanguage, getText("priki.admin.i18n.language"))
    	||  !checkString(i18nKey, getText("priki.admin.i18n.key"))		) {
            return ERROR;
        }
        
    	RemoveI18NTransaction trans = new RemoveI18NTransaction(i18nLanguage, i18nKey);
    	Prevalence.getInstance().execute(trans);
    	 
    	return SUCCESS;
    }
    
    public String showI18N() {
        siteName  = getAdmin().getSiteName();
        adminBasePath  = getAdmin().getBasePath();
        slogan = getAdmin().getSlogan();   
        defaultI18n = getAdmin().getDefaultI18n();
    	
    	I18N i18n = Prevalence.wiki().getAdmin().getI18NOverride(i18nLanguage, i18nKey);
    	
    	if (i18n == null) {
    		i18nLanguage = "";
    		i18nNewLanguage = "";
    		i18nKey = "";
    		i18nNewKey = "";
    		i18nText = "";
    	} else {
    		i18nLanguage = i18n.getLanguage();
    		i18nNewLanguage = i18n.getLanguage();
    		i18nKey = i18n.getKey();
    		i18nNewKey = i18n.getKey();
    		i18nText = i18n.getText();
    	}
	 
    	return SUCCESS;
    }
       
    public String updateI18N() {
    	if (StringUtils.isNullAndBlank(i18nLanguage)
    	 || StringUtils.isNullAndBlank(i18nKey)) {
        	AddI18NTransaction trans = new AddI18NTransaction(i18nNewLanguage, i18nNewKey, i18nText);
        	Prevalence.getInstance().execute(trans);
    	}else{
    		EditI18NTransaction trans = new EditI18NTransaction(i18nLanguage, i18nKey, i18nNewLanguage, i18nNewKey, i18nText);
    		Prevalence.getInstance().execute(trans);
    	}
    	 
    	return SUCCESS;
    }
    
    public String fixWikiRefers() {
      	FixWikiRefersTransaction trans = new FixWikiRefersTransaction();
       	Prevalence.getInstance().execute(trans);
    	return SUCCESS;
    }
    
	public String getAcceptAnonymousEditor() {
		return acceptAnonymousEditor;
	}
	public void setAcceptAnonymousEditor(String acceptAnonymousEditor) {
		this.acceptAnonymousEditor = acceptAnonymousEditor;
	}
	public String getAcceptAnonymousReader() {
		return acceptAnonymousReader;
	}
	public void setAcceptAnonymousReader(String acceptAnonymousReader) {
		this.acceptAnonymousReader = acceptAnonymousReader;
	}    
    
	// --------------------------------
    public String getAdminBasePath() { return this.adminBasePath; }
    public void setAdminBasePath(String basePath) { this.adminBasePath = basePath; }

    public Integer getLastChangesCount() { return this.lastChangesCount; }
    public void setLastChangesCount(Integer lastChangesCount) { this.lastChangesCount = lastChangesCount; }

    public String getSiteName() { return this.siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getSlogan() { return this.slogan; }
    public void setSlogan(String slogan) { this.slogan = slogan; }
    
    public String getRollbackDate() { return this.rollbackDate; }
    public void setRollbackDate(String rollback) { this.rollbackDate = rollback; }
    
    public String getUserIP() { return this.userIP; }
    public void setUserIP(String userIP) { this.userIP = userIP; }
    
    public String getReadonly() { return this.readonly; }
    public void setReadonly(String readonly) { this.readonly = readonly; }

    public String getLastChangesInItems() { return this.lastChangesInItems; }
    public void setLastChangesInItems(String lastAsItems) { this.lastChangesInItems = lastAsItems; }
    
    public String getLastChangesOnlyNewPages() { return this.lastChangesOnlyNewPages; }
    public void setLastChangesOnlyNewPages(String lastChangesOnlyNewPages) { this.lastChangesOnlyNewPages = lastChangesOnlyNewPages; }    
    
    public void setLink(String link) { this.link = link; }
    
	public String getSmtpHost() { return smtpHost; }
	public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
	
	public String getSmtpUser() { return smtpUser; }
	public void setSmtpUser(String smtpUser) { this.smtpUser = smtpUser; }
	
	public String getSmtpPassword() { return smtpPassword; }
	public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }
	
	public String getSmtpPort() { return smtpPort; }
	public void setSmtpPort(String smtpPort) { this.smtpPort = smtpPort; }
	
	public String getFromMail() { return fromMail; }
	public void setFromMail(String fromMail) {this.fromMail = fromMail; }
	
	public String getFromName() { return fromName; }
	public void setFromName(String fromName) { this.fromName = fromName; }    
    
	public List<User> getUsers() {
		return users;
	}
	public Integer getSignUpAs() {
		return signUpAs;
	}
	public void setSignUpAs(Integer signUpAs) {
		this.signUpAs = signUpAs;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getUserIsAdmin() {
		return userIsAdmin;
	}
	public void setUserIsAdmin(String userIsAdmin) {
		this.userIsAdmin = userIsAdmin;
	}
	public String getUserIsEditor() {
		return userIsEditor;
	}
	public void setUserIsEditor(String userIsEditor) {
		this.userIsEditor = userIsEditor;
	}
	public String getUserEscortWiki() {
		return userEscortWiki;
	}
	public void setUserEscortWiki(String userEscortWiki) {
		this.userEscortWiki = userEscortWiki;
	}
	public String getUserIsReader() {
		return userIsReader;
	}
	public void setUserIsReader(String userIsReader) {
		this.userIsReader = userIsReader;
	}
	public String getUserCompleteName() {
		return userCompleteName;
	}
	public void setUserCompleteName(String userCompleteName) {
		this.userCompleteName = userCompleteName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
    public Integer getCommentIndex() {
		return commentIndex;
	}
	public void setCommentIndex(Integer commentIndex) {
		this.commentIndex = commentIndex;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getDefaultI18n() {
		return defaultI18n;
	}
	public void setDefaultI18n(String defaultI18n) {
		this.defaultI18n = defaultI18n;
	}
	public Integer getDefaultVisibility() {
		return defaultVisibility;
	}
	public void setDefaultVisibility(Integer defaultVisibility) {
		this.defaultVisibility = defaultVisibility;
	}
	public String getDefaultCaseSensitive() {
		return defaultCaseSensitive;
	}
	public void setDefaultCaseSensitive(String defaultCaseSensitive) {
		this.defaultCaseSensitive = defaultCaseSensitive;
	}
	public String getPluginName() {
		return pluginName;
	}
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}
	public String getPluginNewName() {
		return pluginNewName;
	}
	public void setPluginNewName(String pluginNewName) {
		this.pluginNewName = pluginNewName;
	}
	public Integer getPluginPosition() {
		return pluginPosition;
	}
	public void setPluginPosition(Integer pluginPosition) {
		this.pluginPosition = pluginPosition;
	}
	public String getPluginHTML() {
		return pluginHTML;
	}
	public void setPluginHTML(String pluginHTML) {
		this.pluginHTML = pluginHTML;
	}
	public Integer getPluginOrder() {
		return pluginOrder;
	}
	public void setPluginOrder(Integer pluginOrder) {
		this.pluginOrder = pluginOrder;
	}
	public String getI18nLanguage() {
		return i18nLanguage;
	}
	public void setI18nLanguage(String language) {
		i18nLanguage = language;
	}
	public String getI18nNewLanguage() {
		return i18nNewLanguage;
	}
	public void setI18nNewLanguage(String newLanguage) {
		i18nNewLanguage = newLanguage;
	}
	public String getI18nKey() {
		return i18nKey;
	}
	public void setI18nKey(String key) {
		i18nKey = key;
	}
	public String getI18nNewKey() {
		return i18nNewKey;
	}
	public void setI18nNewKey(String newKey) {
		i18nNewKey = newKey;
	}
	public String getI18nText() {
		return i18nText;
	}
	public void setI18nText(String text) {
		i18nText = text;
	}
	
    public List<String> getUserEscortingPages() {
		return userEscortingPages;
	}	
}
