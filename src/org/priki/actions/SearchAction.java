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
import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;
import org.priki.bo.Wikiword;
import org.priki.format.FormatPrevalenceFactory;
import org.priki.format.WikiwordsValidator;
import org.priki.service.Prevalence;

public class SearchAction extends SuperAction {

	public static String SHOW_WIKIWORD = "show_wikiword";
	
    private String keyword;
    
    private WikiwordsValidator validator = FormatPrevalenceFactory.createDefaultWikiwordsValidator(Prevalence.wiki());
    
    private Collection<Wikiword> searchResults;
    
    //---------------------------------------------------------------------
    public Collection<Wikiword> getSearchResults() { return this.searchResults; }
    
    public String getKeyword() { return this.keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    
    // ---------------------------------------------------------------------
    
    public String search() { 
    	keyword = StringEscapeUtils.unescapeHtml(keyword.trim());
    	
    	
    	/* We doesn't need this
        if (!validator.matchWikiwords(keyword)) {
        	setErrorMessage("priki.invalidKeyword", keyword);
            return ERROR;
        }*/
    	
    	if (!checkString(keyword, getText("priki.keyword"))) {
            return ERROR;
        }
        
        searchResults = Prevalence.wiki().search(keyword);
        
        Wikiword e = Prevalence.wiki().getWikiword(keyword);
        
        if (e == null || !e.hasDefinition()) {
            return SUCCESS;
        }

        try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			setErrorMessage("priki.invalidConversionUTF8", keyword);
			return ERROR;
		}
        
        return SHOW_WIKIWORD;
    }
}
