package com.siemens.cto.aem.ws.rest.v1.service.user.impl;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.user.UserServiceRest;

/**
 * @author Cerner
 *
 */
public class UserServiceRestImpl implements UserServiceRest {

    @Autowired
    AuthenticationConfiguration authenticationConfiguration;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceRestImpl.class);

    public static final String JSON_RESPONSE_OK = "{'response':'ok'}";
    public static final String JSON_RESPONSE_TRUE = "{'response':'true'}";
    public static final String JSON_RESPONSE_FALSE = "{'response':'false'}";

    private static final String TOC_AUTHORIZATION= "toc.authorization";
    private static final String PROP_TOC_ROLE_ADMIN = "toc.role.admin";
    
    private static final String USER = "user";

    @Override
    public Response login(HttpServletRequest request, String userName, String password) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken( userName, password );
            Authentication result = authenticationConfiguration.getAuthenticationManager().authenticate( authRequest );
            SecurityContextHolder.getContext().setAuthentication( result );
        } catch (Exception e) {
            LOGGER.error("Error Login",
                         e);
            return ResponseBuilder.notOk(Response.Status.UNAUTHORIZED,
                                         new FaultCodeException(AemFaultType.USER_AUTHENTICATION_FAILED,
                                                                e.getMessage()));
        }

        request.getSession().setAttribute(USER, userName);
        return ResponseBuilder.ok(JSON_RESPONSE_OK);
    }

    @Override
    public Response logout(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.debug("Entered logout for user: {}", request.getUserPrincipal());
        request.getSession().invalidate();
        return ResponseBuilder.ok(JSON_RESPONSE_OK);
    }

    @Override
    public Response isUserAdmin(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            @SuppressWarnings("unchecked")
            Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) auth.getAuthorities();
            if (authorities!= null &&  authorities.contains(new SimpleGrantedAuthority(ApplicationProperties.get(PROP_TOC_ROLE_ADMIN)))) {
                return ResponseBuilder.ok(JSON_RESPONSE_TRUE);
            }
        }
        return ResponseBuilder.ok(JSON_RESPONSE_FALSE);
    }

    /* (non-Javadoc)
     * @see com.siemens.cto.aem.ws.rest.v1.service.user.UserServiceRest#isTOCAuthorizationEnabled(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Response isTOCAuthorizationEnabled(HttpServletRequest request, HttpServletResponse response) {
        String auth = ApplicationProperties.get(TOC_AUTHORIZATION, "true");
        if("false".equals(auth))
            return ResponseBuilder.ok(JSON_RESPONSE_FALSE);
        else 
            return ResponseBuilder.ok(JSON_RESPONSE_TRUE);
    }
    
    
}