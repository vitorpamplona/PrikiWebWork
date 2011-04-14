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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.priki.bo.Text;
import org.priki.bo.User;
import org.priki.bo.Wikiword;
import org.priki.format.FormatPrevalenceFactory;
import org.priki.format.WikiwordsValidator;
import org.priki.service.Prevalence;

public class HistoryAction extends SuperAction {

    private String keyword;
    private Integer historyId;
    private String postUser;
    private String completeUserName;
    private List<String> userEscortingPages;
    
    private Map<Integer, Text> history;
    private Map<Date, Wikiword> lastChanged;
    
    private WikiwordsValidator validator = FormatPrevalenceFactory.createDefaultWikiwordsValidator(Prevalence.wiki());
    
    //---------------------------------------------------------------------
    public Map<Integer, Text> getHistory() { return this.history; }
    public Map<Date, Wikiword> getLastChanged() { return this.lastChanged; }
    
    public List<String> getUserEscortingPages() { return userEscortingPages; }
    
    public String getCompleteUserName() { return this.completeUserName; }
    
    public String getKeyword() { return this.keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getHistoryId() { return this.historyId;}
    public void setHistoryId(Integer id) { this.historyId = id; }
        
    public String getPostUser() { return postUser; }
    public void setPostUser(String user) { this.postUser = user; }
    // ---------------------------------------------------------------------
    
    public String history() {
       // if (!validator.matchWikiwords(keyword)) {
       // 	setErrorMessage("priki.invalidKeyword", keyword);
       //     return ERROR;
       // }
        
        Wikiword e = Prevalence.wiki().getWikiword(keyword);
               
        if (e == null || !e.hasDefinition()) {
            return SUCCESS;
        }
        
        history = new TreeMap<Integer, Text>();
        for (int i=0; i<e.getHistoryCount(); i++) {
            history.put(i, e.getHistory(i));
        }
        
        return SUCCESS;
    }
    
    public String userHistory() {
        if (postUser == null || postUser.trim().equals("")) {
        	setErrorMessage("priki.userNotFound", postUser);
            return ERROR;
        }
        
        User reggistered = Prevalence.wiki().getAdmin().getAccessManager().getUser(postUser);
        
		userEscortingPages = new ArrayList<String>();
		for (String w : Prevalence.wiki().getWikiwords()) {
			if (Prevalence.wiki().getWikiword(w).getEscortWikiWord().contains(reggistered)) {
				userEscortingPages.add(w);
			}
		}
        
        lastChanged = Prevalence.wiki().changedByUser(postUser);
        completeUserName = reggistered.getCompleteName();
        
        return SUCCESS;
    }
    
}
