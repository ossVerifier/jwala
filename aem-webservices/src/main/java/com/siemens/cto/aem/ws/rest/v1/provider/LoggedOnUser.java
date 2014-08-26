package com.siemens.cto.aem.ws.rest.v1.provider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.springframework.stereotype.Component;

import com.siemens.cto.aem.domain.model.temporary.User;

@Component
public class LoggedOnUser {
    
    @Context
    SecurityContext context;

    private User user;
    
    public LoggedOnUser() {        
        if(context != null && context.getUserPrincipal() != null) {
            this.user = new User(context.getUserPrincipal().getName()); 
        } else {
            this.user = User.getHardCodedUser();
        }
    }

    public User getUser() {
        return user;
    }

    public static User fromContext(SecurityContext jaxrsSecurityContext) {
        return new User(jaxrsSecurityContext.getUserPrincipal().getName()); 
    }
}
