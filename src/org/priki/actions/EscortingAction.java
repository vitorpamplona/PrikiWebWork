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

import org.apache.commons.lang.StringEscapeUtils;
import org.priki.bo.User;
import org.priki.format.FormatPrevalenceFactory;
import org.priki.format.WikiwordsValidator;
import org.priki.prevalence.AddEscortWikiwordTransaction;
import org.priki.prevalence.RemoveEscortWikiwordTransaction;
import org.priki.service.AccessManagerBean;
import org.priki.service.Prevalence;

public class EscortingAction extends SuperAction {

    private String keyword;

    private WikiwordsValidator validator = FormatPrevalenceFactory.createDefaultWikiwordsValidator(Prevalence.wiki());
    
    public String getKeyword() { return this.keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
   
    // ---------------------------------------------------------------------
    
    public String escortWikiword() {
    	keyword = StringEscapeUtils.unescapeHtml(keyword.trim());
    	
    	String loggedUser = (String) session.getUserLogged();
    	User u = Prevalence.wiki().getAdmin().getAccessManager().getUser(loggedUser);
    	
        if (u == null) {
        	
        	setErrorMessage("priki.loggedIn");
            return ERROR;
        }
        
        //if (!validator.matchWikiwords(keyword)) {
       // 	setErrorMessage("priki.invalidKeyword", keyword);
        //    return ERROR;
        //}
        
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
    	
    	String loggedUser = (String) session.getUserLogged();
    	User u = Prevalence.wiki().getAdmin().getAccessManager().getUser(loggedUser);
    	
        if (u == null) {
        	setErrorMessage("priki.loggedIn");
            return ERROR;
        }
        
        if (!new AccessManagerBean().isCanRead(keyword)) {
        	setErrorMessage(getText("priki.permissions.editor"));
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
}
