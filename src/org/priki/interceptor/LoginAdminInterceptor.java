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
package org.priki.interceptor;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;

/**
 * $Id: LoginAdminInterceptor.java,v 1.2 2005/09/15 01:55:39 vfpamp Exp $
 * @author Dalton Camargo - <a href="mailto:dalton@javabb.org">dalton@javabb.org </a> <br>
 */
public class LoginAdminInterceptor extends AroundInterceptor {

    private boolean loggedIn = false;

    /**
     * @param invocation
     * @throws Exception
     * @see com.opensymphony.xwork.interceptor.AroundInterceptor#before(com.opensymphony.xwork.ActionInvocation)
     */
    protected void before(ActionInvocation invocation) throws Exception {
        ActionContext ctx = ActionContext.getContext();
        Map session = ctx.getSession();
        String userAdmin = (String) session.get("userAdmin");
        
        if ((userAdmin != null) && (!"".equals(userAdmin))) {
        	loggedIn = true;
        } else {
            loggedIn = false;
        }
    }

    /**
     * @param invocation
     * @param result
     * @throws Exception
     * @see com.opensymphony.xwork.interceptor.AroundInterceptor#after(com.opensymphony.xwork.ActionInvocation,
     * java.lang.String)
     */
    protected void after(ActionInvocation invocation, String result) throws Exception {
    }

    /**
     * @param invocation
     * @return result
     * @throws Exception
     * @see com.opensymphony.xwork.interceptor.Interceptor#intercept(com.opensymphony.xwork.ActionInvocation)
     */
    public String intercept(ActionInvocation invocation) throws Exception {
        before(invocation);

        if (loggedIn == false) {
            return Action.LOGIN;
        }

        String result = invocation.invoke();
        after(invocation, result);

        return result;
    }
}
