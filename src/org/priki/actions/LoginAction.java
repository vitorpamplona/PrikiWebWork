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
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.RandomStringUtils;
import org.priki.bo.User;
import org.priki.interceptor.LoginCookieInterceptor;
import org.priki.prevalence.SetUserCookieTransaction;
import org.priki.service.Mail;
import org.priki.service.Prevalence;

import com.opensymphony.webwork.ServletActionContext;

/**
 * $Id: LoginAction.java,v 1.2 2005/09/15 01:55:16 vfpamp Exp $
 * @author Dalton Camargo - <a href="mailto:dalton@javafree.com.br">dalton@javafree.com.br</a> <br>
 * @author Lucas Teixeira - <a href="mailto:lucas@javafree.com.br">lucas@javafree.com.br</a> <br>
 */
public class LoginAction extends SuperAction {

	private String user = "";
	private String password = "";
	private String useCookie = "";
	private String email = "";
	
	public String login() throws Exception {
		boolean bUseCookie = "useCookie".equals(useCookie);
		
		if(Prevalence.wiki().getAdmin().getAccessManager().checkLogin(user, password)){
			User userLogged = Prevalence.wiki().getAdmin().getAccessManager().getUser(user);
			if (userLogged == null)
				userLogged = Prevalence.wiki().getAdmin().getAccessManager().getUserByEmail(user);
			
			session.logoutAdmin();
			session.logoutUser();
			session.loginUser(userLogged.getIdentifier(), userLogged.getPassword());
						
			if (Prevalence.wiki().getAdmin().getAccessManager().isAdmin(userLogged.getIdentifier())) {
				session.loginAdmin(userLogged.getIdentifier());
			}
				
			if (bUseCookie) {
				String hash = RandomStringUtils.randomAlphanumeric(300);
				SetUserCookieTransaction trans = new SetUserCookieTransaction(userLogged.getIdentifier(), hash);
				Prevalence.getInstance().execute(trans);
				
    			Cookie c = new Cookie(LoginCookieInterceptor.USER_HASH_ID, hash);
    			c.setMaxAge(2243200);
    			ServletActionContext.getResponse().addCookie(c);				
			}		
			
			return SUCCESS;
		} 
		return LOGIN;
	}
	
	public String logout()  throws Exception {
		Cookie c = new Cookie(LoginCookieInterceptor.USER_HASH_ID, null);
		c.setMaxAge(0);
		ServletActionContext.getResponse().addCookie(c);						
		
		session.logoutAdmin();
		session.logoutUser();
		return SUCCESS;
	}
	
	public String forgot() throws Exception {

		User user = Prevalence.wiki().getAdmin().getAccessManager().getUserByEmail(email);
		
		if (user == null) {
			setErrorMessage("priki.forgotMyUser.emailNotFound", email);
			return ERROR;
		}
		
		List<String> params = new ArrayList<String>();
		params.add(Prevalence.wiki().getAdmin().getSiteName());
		
		String subject = getText("priki.forgotMyUser.emailSubject", params);
		
		params.add(user.getIdentifier());
		params.add(user.getPassword());
		params.add(user.getCompleteName());
		params.add(Prevalence.wiki().getAdmin().getSlogan());
		params.add(Prevalence.wiki().getAdmin().getBasePath());
		
		String bodyMessage = getText("priki.forgotMyUser.emailText", params);
		
		Mail mail = new Mail(Prevalence.wiki().getAdmin().getMailConfiguration());				
		
		mail.sendMail(user.getEmail(), subject, bodyMessage);		
		
		return SUCCESS;
	}
	
	public String index()  throws Exception {
		return SUCCESS;
	}
	
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getUseCookie() { return useCookie; }
	public void setUseCookie(String useCookie) { this.useCookie = useCookie; }
}
