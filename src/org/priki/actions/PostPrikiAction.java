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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.priki.bo.User;
import org.priki.bo.Wikiword;
import org.priki.format.FormatPrevalenceFactory;
import org.priki.format.Formatter;
import org.priki.format.WikiwordsValidator;
import org.priki.prevalence.AddCommentTransaction;
import org.priki.prevalence.ParserTransaction;
import org.priki.prevalence.exceptions.PoliceException;
import org.priki.service.AccessManagerBean;
import org.priki.service.EscortingWiki;
import org.priki.service.Prevalence;
import org.priki.utils.StringUtils;

public class PostPrikiAction extends SuperAction {

    private String keyword;
    private String newKeyword;// Used to rename  
    private String definitionOnlyText;
    private Integer visibility;
    private String caseSensitive;
    
    private String postUserName;
    private String commaSeparatedTags;
    private String commentOnlyText;
    
    private WikiwordsValidator validator = FormatPrevalenceFactory.createDefaultWikiwordsValidator(Prevalence.wiki());
    private Formatter format = FormatPrevalenceFactory.createDefaultFormatter(Prevalence.wiki());
    
    public void setDefinitionOnlyText(String definition) { this.definitionOnlyText = definition; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public void setNewKeyword(String keyword) { this.newKeyword = keyword; }
    public void setCommaSeparatedTags(String tags) { this.commaSeparatedTags = tags; }
    public void setPostUserName(String userName) { postUserName = userName; }
    public void setCaseSensitive(String caseSensitive) { this.caseSensitive = caseSensitive; }
	public void setCommentOnlyText(String commentOnlyText) { this.commentOnlyText = commentOnlyText; }
	public void setVisibility(Integer visibility) { this.visibility = visibility; }
    
    public String getKeyword() { return this.keyword; }
    // ---------------------------------------------------------------------
    
    private List<String> getTags() {
    	String[] buffer = commaSeparatedTags.split(",");
    	List<String> ret = new ArrayList<String>();
    	
    	for (String temp : buffer) {
    		temp = temp.trim();
    		
    		if (!validator.matchWikiwords(temp)) {
    			setErrorMessage("priki.invalidTag", temp);
    			return null;
    		}
    		
    		if (temp.length() > 0)
    			ret.add(temp);
    	}
    	
        return ret;
    }
    
    public String postWikiword() {
        if (StringUtils.isNullAndBlank(keyword) && newKeyword != null) {
        	keyword = newKeyword;
        }
                
        // NEEDED BY HTML CODE INSIDE THE POST OF THE USER
        definitionOnlyText = definitionOnlyText.replaceAll("&lt;", "&menor;").replaceAll("&gt;", "&maior;");
        
        // Remove HTML encoding &altide;
        keyword = StringEscapeUtils.unescapeHtml(keyword.trim());
        newKeyword = StringEscapeUtils.unescapeHtml(newKeyword.trim());
        definitionOnlyText = StringEscapeUtils.unescapeHtml(definitionOnlyText);

        // NEEDED BY HTML CODE INSIDE THE POST OF THE USER
        definitionOnlyText = definitionOnlyText.replaceAll("&menor;", "&amp;lt;").replaceAll("&maior;", "&amp;gt;");
        
        if (!checkInteger(this.visibility, getText("priki.visibility"))) {
        	return ERROR;
        }
        
        if (!isAValidKeyword(keyword)) {
        	setErrorMessage("priki.invalidKeyword", keyword);
        	return ERROR;
    	}
        
        //if (!checkString(definitionOnlyText, getText("priki.text"))) {
        //    return ERROR;
        //}
        
        if (!isFloodOK()) {
            setErrorMessage(getText("priki.captcha.incorrectCode"));
            return ERROR;
        }
        
        if (isReadonlyWiki()) {
            setErrorMessage(getText("priki.readonlyMessage"));
            return ERROR;
        }
        
        if (!new AccessManagerBean().isCanPost(keyword)) {
        	setErrorMessage(getText("priki.permissions.editor"));
            return ERROR;
        }

        //if (!validator.matchWikiwords(keyword)) {
        //	setErrorMessage("priki.invalidKeyword", keyword);
        //    return ERROR;
        //}
        
        List<String> tags = getTags();

        if (tags == null) {
            return ERROR;
        }
        
        // Check conflicting wikiword
        boolean sensitive = Boolean.valueOf(caseSensitive);
        Collection<Wikiword> conflicting = validator.getConflictingWikiwords(keyword, sensitive);
        if (!conflicting.isEmpty()) {
            setErrorMessage(getConflictingMessage(keyword, sensitive, conflicting));
            return ERROR;
        }

        String user = getUserIP();
        String loggedUser = (String) session.getUserLogged();
        
        if (loggedUser!= null && loggedUser.length() > 0) {
        	user = loggedUser;
        }
        
        Wikiword.Visibility vis;
        vis = Wikiword.Visibility.values()[this.visibility.intValue()];
        
        // Calling Prevayler.
        try {
            ParserTransaction trans = new ParserTransaction(user, keyword, newKeyword, definitionOnlyText, sensitive, vis, tags);
            Prevalence.getInstance().execute(trans);
        } catch (PoliceException e) {
        	setErrorMessage("priki.invalidText", newKeyword);
            return ERROR;
        }
                
        keyword = newKeyword;

		notifyEdit(user, keyword);
        
        try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			setErrorMessage("priki.invalidConversionUTF8", keyword);
			return ERROR;
		}
        
        return SUCCESS;
    
    }
    
    private void notifyEdit(String user, String keyword) {
    	Wikiword word = Prevalence.wiki().getWikiword(keyword);
    	String text = format.format(word.getDefinition());
    	text = text.replaceAll("&amp; ", "&").replaceAll("&amp;", "&");
    	User editor = Prevalence.wiki().getAdmin().getAccessManager().getUser(user);
    	
    	List<String> params = new ArrayList<String>();
		params.add(Prevalence.wiki().getAdmin().getSiteName());
		params.add(keyword);
		
		String subject = getText("priki.escortingWiki.emailSubject", params);
		
		// changes the pattern
		params.add(EscortingWiki.USER_PATTERN);
		params.add(editor != null ? editor.getName() : user);
		params.add(text);
		params.add(Prevalence.wiki().getAdmin().getSlogan());
		params.add(Prevalence.wiki().getAdmin().getBasePath());
		
		String bodyMessage = getText("priki.escortingWiki.emailText", params);		
		
		EscortingWiki.getInstance().notifyEscorting(subject, bodyMessage, word, editor);
    }
    
    private void notifyComment(String user, String keyword, String comment) {
    	User editor = Prevalence.wiki().getAdmin().getAccessManager().getUser(user);
    	
    	List<String> params = new ArrayList<String>();
		params.add(Prevalence.wiki().getAdmin().getSiteName());
		params.add(keyword);
		
		String subject = getText("priki.escortingWikiComment.emailSubject", params);
		
		// changes the pattern
		params.add(EscortingWiki.USER_PATTERN);
		params.add(editor != null ? editor.getName() : user);
		params.add(comment);
		params.add(Prevalence.wiki().getAdmin().getSlogan());
		params.add(Prevalence.wiki().getAdmin().getBasePath());
		
		String bodyMessage = getText("priki.escortingWikiComment.emailText", params);		
		
		EscortingWiki.getInstance().notifyEscorting(subject, bodyMessage, Prevalence.wiki().getWikiword(keyword), editor);
    }
    
    private static String getConflictingMessage(String keyword, boolean sensitive, Collection<Wikiword> conflicting) {
        StringBuilder message = new StringBuilder();
        message.append("Keyword ");
        message.append(keyword);
        message.append(' ');
        message.append(sensitive ? "(sensitive)" : "(insensitive)");
        message.append(" conflicts with:\n");
        for (Wikiword word : conflicting) {
            message.append(word.getKeyword());
            message.append(' ');
            message.append(word.isCaseSensitive() ? "(sensitive)" : "(insensitive)");
            message.append('\n');
        }
        return message.toString();
    }
    
    public String postComment() {
        // NEEDED BY HTML CODE INSIDE THE POST OF THE USER
    	commentOnlyText = commentOnlyText.replaceAll("\n", "<BR>");
    	commentOnlyText = commentOnlyText.replaceAll("&lt;", "&menor;").replaceAll("&gt;", "&maior;");

    	if (!StringUtils.isNullAndBlank(postUserName)) {
    		commentOnlyText += "<BR><BR>-- " + postUserName;
    	}
    	
        // Remove HTML encoding &altide;
        keyword = StringEscapeUtils.unescapeHtml(this.keyword);
        commentOnlyText = StringEscapeUtils.unescapeHtml(commentOnlyText);

        // NEEDED BY HTML CODE INSIDE THE POST OF THE USER
        commentOnlyText = commentOnlyText.replaceAll("&menor;", "&amp;lt;").replaceAll("&maior;", "&amp;gt;");
        
        if (!checkString(commentOnlyText, getText("priki.commentText"))) {
            return ERROR;
        }
        
        if (!isFloodOK()) {
        	setErrorMessage(getText("priki.captcha.incorrectCode"));
            return ERROR;
        }
        
        if (isReadonlyWiki()) {
            setErrorMessage(getText("priki.readonlyMessage"));
            return ERROR;
        }

        //if (!validator.matchWikiwords(keyword)) {
        //    setErrorMessage("priki.invalidKeyword", keyword);
        //    return ERROR;
        //}

        String user = getUserIP();
        String loggedUser = (String) session.getUserLogged();
        
        if (loggedUser!= null && loggedUser.length() > 0) {
        	user = loggedUser;
        }
        
        // Calling Prevayler.
        try {
        	AddCommentTransaction trans = new AddCommentTransaction(user, keyword, commentOnlyText);
            Prevalence.getInstance().execute(trans);
        } catch (PoliceException e) {
        	setErrorMessage("priki.invalidKeyword", keyword);
            return ERROR;
        }

		notifyComment(user, keyword, commentOnlyText);
        
        try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			setErrorMessage("priki.invalidConversionUTF-8", keyword);
			return ERROR;
		}
        
        return SUCCESS;
    }    
}
