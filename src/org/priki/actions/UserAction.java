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

import org.priki.bo.User;
import org.priki.prevalence.AddUserTransaction;
import org.priki.prevalence.EditUserTransaction;
import org.priki.service.Prevalence;
import org.priki.utils.StringUtils;

/**
 * $Id: LoginAction.java,v 1.2 2005/09/15 01:55:16 vfpamp Exp $
 * @author Dalton Camargo - <a href="mailto:dalton@javafree.com.br">dalton@javafree.com.br</a> <br>
 * @author Lucas Teixeira - <a href="mailto:lucas@javafree.com.br">lucas@javafree.com.br</a> <br>
 */
public class UserAction extends SuperAction {

	private String user = "";
	private String oldPassword = "";
	private String newPassword = "";
	private String completeName = "";
	private String email = "";
	private String escortWiki;
	
	public String showUser() throws Exception {
		String loggedUser = (String) session.getUserLogged();
		String loggedPass = (String) session.getUserPassword();
		
		if (StringUtils.isNullAndBlank(loggedPass)
		 || StringUtils.isNullAndBlank(loggedUser)		) {
			setErrorMessage(getText("priki.loggedIn"));
			return ERROR;
		}
		
		User logged = Prevalence.wiki().getAdmin().getAccessManager().getUser(loggedUser);
		
		if (logged == null) {
			setErrorMessage(getText("priki.loggedWithANotFoundUser"));
			return ERROR;
		}
		
		user = loggedUser;
		completeName = logged.getCompleteName();
		email = logged.getEmail();
		escortWiki = logged.isEscortWiki() ? getText("priki.yes") : getText("priki.no");
		
		return SUCCESS;
	}
	
	public String signUp() throws Exception {
		if ((!checkString(user, getText("priki.userProfile.user")))
		||	(!checkString(newPassword, getText("priki.userProfile.password")))
		||	(!checkString(completeName, getText("priki.userProfile.name")))
		||	(!checkString(email, getText("priki.userProfile.email")))) {
			return ERROR;
		}		
		
        if (!isFloodOK()) {
            setErrorMessage(getText("priki.captcha.incorrectCode"));
            return ERROR;
        }
		
		if (Prevalence.wiki().getAdmin().getAccessManager().isUser(user)) {
			setErrorMessage(getText("priki.userAlreadyRegistered"));
			return ERROR;
		}
		
        // Calling Prevayler.
        AddUserTransaction trans = new AddUserTransaction(user.trim(), newPassword, completeName, email);
        Prevalence.getInstance().execute(trans);
		
        session.loginUser(user, newPassword);
        
		return SUCCESS;
	}
	
	public String postUser() throws Exception {
		String loggedUser = (String) session.getUserLogged();
		String loggedPass = (String) session.getUserPassword();
		
		if (StringUtils.isNullAndBlank(oldPassword) 
		 || StringUtils.isNullAndBlank(newPassword)) {
			oldPassword = loggedPass;
			newPassword = loggedPass;
		}
		
		if ((!checkString(loggedUser, getText("priki.userProfile.user")))
		||	(!checkString(loggedPass, getText("priki.userProfile.password")))
		||	(!checkString(completeName, getText("priki.userProfile.name")))
		||	(!checkString(email, getText("priki.userProfile.email")))) {
			return ERROR;
		}

		if (!oldPassword.equals(loggedPass)) {
			setErrorMessage(getText("priki.wrongOldPassword"));
			return ERROR;			
		}
		
		if (!Prevalence.wiki().getAdmin().getAccessManager().isUser(loggedUser)) {
			setErrorMessage(getText("priki.userNotFound"));
			return ERROR;
		}
		
		boolean bEscortWiki = "escortWiki".equals(escortWiki);
		
        // Calling Prevayler.
        EditUserTransaction trans = new EditUserTransaction(loggedUser, oldPassword, newPassword, completeName, email, bEscortWiki);
        Prevalence.getInstance().execute(trans);
		
		return SUCCESS;
	}	

	public String showSignUp() {
		return SUCCESS;
	}
	
	public String getCompleteName() { return completeName; }
	public void setCompleteName(String completeName) { this.completeName = completeName; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getNewPassword() { return newPassword; }
	public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

	public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }

	public String getEscortWiki() {
		return escortWiki;
	}

	public void setEscortWiki(String escortWiki) {
		this.escortWiki = escortWiki;
	}
}
