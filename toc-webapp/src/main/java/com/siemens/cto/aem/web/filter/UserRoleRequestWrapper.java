package com.siemens.cto.aem.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

/**
 * Request wrapper which sole purpose is to pass the currently logged in user via getUserPrincipal which is required
 * when accessing secured Tomcat resources like the the Tomcat Manager GUI.
 *
 * Created by Z003BPEJ on 8/6/14.
 */
public class UserRoleRequestWrapper extends HttpServletRequestWrapper {
    final String user;

    public UserRoleRequestWrapper(final HttpServletRequest request, final String aUser) {
        super(request);
        this.user = aUser;
    }

    @Override
    public Principal getUserPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return user;
            }
        };
    }
}