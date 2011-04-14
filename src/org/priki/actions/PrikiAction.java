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
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.priki.bo.Text;
import org.priki.bo.User;
import org.priki.bo.Wikiword;
import org.priki.format.FormatPrevalenceFactory;
import org.priki.format.Formatter;
import org.priki.format.WikiwordsValidator;
import org.priki.service.Prevalence;
import org.priki.utils.SortedBag;
import org.priki.utils.StringUtils;

public class PrikiAction extends SuperAction {

    private String keyword;
    private String definitionWithLinks;
    private String definitionOnlyText;
    private Integer historyId;
    private Date postDate;
    private String postUser;
    private String postUserName;
    private String caseSensitive;
    private Integer visibility;
    private Boolean escorting;
    private Set<User> escortingUsers;
    
    private SortedBag<Wikiword> related;
    private SortedBag<Wikiword> tags;
    private String commaSeparatedTags;
    
    private WikiwordsValidator validator = FormatPrevalenceFactory.createDefaultWikiwordsValidator(Prevalence.wiki());
    private Formatter format = FormatPrevalenceFactory.createDefaultFormatter(Prevalence.wiki());
    
    private List<Text> comments;
    
    //---------------------------------------------------------------------
    public SortedBag<Wikiword> getRelated() { return this.related; }
    public Set<User> getEscortingUsers() { return this.escortingUsers; }
    public SortedBag<Wikiword> getTags() { return this.tags; }
    
    // ---------------------------------------------------------------------
    // Lazy initialization
    // ---------------------------------------------------------------------
    private SortedMap<Date, Wikiword> lastChanged;
    private Integer page;
    private Integer lastPage;
    
    private String getTheUser() {
    	String userLogged = getUserLogged();
    	if (userLogged == null || userLogged.equals("")) {
    		return getUserIP();
    	}
    	return userLogged;
    }
    
    public Map<Date, Wikiword> getLastChanged() {
        if (lastChanged == null) {
        	
        	lastChanged = Prevalence.wiki().lastChangedFull(getTheUser());      	
        
        	Date min = lastChanged.firstKey();
        	Date max = lastChanged.lastKey();
        	max = new Date(max.getTime()-1);
        	
        	int cont=0;
            for (Date temp : lastChanged.keySet()) {
            	if (cont == getPage() * Prevalence.wiki().getAdmin().getLastChangesCount())
            		min = temp;
            	if (cont == (getPage()+1) * Prevalence.wiki().getAdmin().getLastChangesCount())
            		max = temp;
            	cont ++;
            }
            lastChanged = lastChanged.subMap(min, max);
        }
        return lastChanged;
    }    
    
	public Integer getPage() { 
		if (page == null) page = 0;
		if (page < 0) page = 0;
		if (page > getLastPage()) page = getLastPage();
		return page;	
	}
	public Integer getLastPage() {
        if (lastPage == null) {
        	lastPage = (int)
        		Math.ceil(Prevalence.wiki().lastChangedFull(getTheUser()).size() / 
        		(float) Prevalence.wiki().getAdmin().getLastChangesCount()) -1;
        }
        return lastPage;
	}
	
	public void setPage(Integer page) { this.page = page; }
    
    public String getDefinitionOnlyText() { return this.definitionOnlyText; }
    public String getDefinitionWithLinks() { return this.definitionWithLinks; }
    
    public Date getPostDate() { return this.postDate; }
    public String getStrPostDate() { return getDf().format(this.postDate); }
    
    public String getKeyword() { return this.keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCommaSeparatedTags() { return commaSeparatedTags; }
    
    public Integer getHistoryId() { return this.historyId;}
    public void setHistoryId(Integer id) { this.historyId = id; }
        
    public String getPostUser() { return postUser; }
    public void setPostUser(String user) { this.postUser = user; }
    
    public String getPostUserName() { return postUserName; }
    public String getCaseSensitive() { return caseSensitive; }
    
	public List<Text> getComments() { return comments; }
	public Integer getVisibility() { return visibility; }
	public Boolean getEscorting() { return escorting; }
    
    // ---------------------------------------------------------------------
    public String showWikiword() {
        if (StringUtils.isNullAndBlank(keyword)) {
        	keyword = getText("priki.frontPageMenuItem");
        }
    	
    	keyword = StringEscapeUtils.unescapeHtml(keyword.trim());
    	      
        //if (!validator.matchWikiwords(keyword)) {
        //	setErrorMessage("priki.invalidKeyword", keyword);
        //    return ERROR;
        //}
        
        //if (!isCanRead()) {
        //	setErrorMessage("Sorry. Only registered users with the role READ can read posts. Ask the administrator for the role.");
        //    return ERROR;
        //}
        
        Wikiword e = loadWikiword();
        
        if (e == null || !e.hasDefinition()) {
            // Load default values
            caseSensitive = Boolean.toString(true);
            visibility = Wikiword.Visibility.Public.ordinal();
            
            if (e!=null)
                related = e.getRelated();
            return SUCCESS;
        }
        
        Text text = e.getDefinition();

        if (historyId != null) {
            // Openning History. 
            text = e.getHistory(historyId);
            
            if (text == null) {
                setErrorMessage("History not found");
                return ERROR;
            }
        }
        
        postDate = text.getPostDate();
        postUser = text.getWhoPosted().getIdentifier();
        postUserName = text.getWhoPosted().getName();
        caseSensitive = Boolean.toString(e.isCaseSensitive()); 
        definitionWithLinks = format.format(text);
        definitionOnlyText = format.formatWithoutLinks(text);
        visibility = e.getVisibility().ordinal();
        escorting = e.isEscorting(session.getUserLogged());
        
        escortingUsers = e.getEscortWikiWord();
        
        // NEEDED BY HTML CODE INSIDE THE POST OF THE USER
        definitionWithLinks = definitionWithLinks.replaceAll("&amp; ", "&").replaceAll("&amp;", "&");
        definitionOnlyText = definitionOnlyText.replaceAll("&amp; ", "&").replaceAll("&amp;", "&");
        
        if (text.isOnlyALink()) {
        	setRedirectTo(text.getOnlyALink());
        	ArrayList<String> url = new ArrayList<String>();
        	url.add(text.getOnlyALink());
        	definitionWithLinks = getText("priki.redirecting", url) + "<BR><BR>";
        }
        
        comments = e.getComments();
        related = e.getRelated();
        tags = e.getTags();
        
        createCommaSeparatedTags(tags);
        
        return SUCCESS;
    }
    
    private void createCommaSeparatedTags(SortedBag<Wikiword> tags) {
    	StringBuilder builder = new StringBuilder();
    	boolean firstTime = true;
    	for (Wikiword word : tags) {
    		if (!firstTime) { 
    			builder.append(", "); 
    		}
    		builder.append(word.getKeyword());
    		firstTime = false;
    	}
    	
    	commaSeparatedTags = builder.toString();
    }
    
    public Formatter getFormatter() {
    	return format;
    }
    
    public String getWikiwordFormattedWithLinks(String keyword) {
    	Wikiword w = Prevalence.wiki().getWikiword(keyword);
    	
        if (w == null || !w.hasDefinition()) {
            // Load default values
            return "";
        }
        
        String text = format.format(w.getDefinition());
        text  = text.replaceAll("&amp; ", "&").replaceAll("&amp;", "&");
        return text;
    }
    
    public String index() {      
        return SUCCESS;
    }
    
    public String login() {       
        return SUCCESS;
    }
    
    public Integer getDefaultVisibility() {
    	return Prevalence.wiki().getAdmin().getAccessManager().getDefaultVisibility().ordinal();
    }

    public String getDefaultCaseSensitive() {
    	return Boolean.toString(Prevalence.wiki().getAdmin().getAccessManager().isDefaultCaseSensitive());
    }
    
    /** returns the element by keyword */
    private Wikiword loadWikiword() {
        definitionWithLinks = null;
        definitionOnlyText = null;
        Wikiword word = Prevalence.wiki().getWikiword(keyword);
        return word;
    }
}
