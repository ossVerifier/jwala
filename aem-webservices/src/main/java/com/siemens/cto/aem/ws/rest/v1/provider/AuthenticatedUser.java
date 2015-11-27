package com.siemens.cto.aem.ws.rest.v1.provider;

import com.siemens.cto.aem.domain.model.user.User;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

public class AuthenticatedUser {

    @Context
    private SecurityContext context;

    public AuthenticatedUser() {
    }

    public AuthenticatedUser(final SecurityContext theSecurityContext) {
        context = theSecurityContext;
    }

    public User getUser() {
        if(context.getUserPrincipal() == null) { return new User("nouser"); } // do not check in
        //TODO This should throw some sort of security exception if there's nobody logged in
        return new User(context.getUserPrincipal().getName());
    }
}
