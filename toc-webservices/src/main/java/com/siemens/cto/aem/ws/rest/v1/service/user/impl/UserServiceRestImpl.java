package com.siemens.cto.aem.ws.rest.v1.service.user.impl;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.user.UserServiceRest;
import com.sun.jndi.ldap.LdapCtxFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.Hashtable;

/**
 * Created by z002xuvs on 5/29/2014.
 */
public class UserServiceRestImpl implements UserServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceRestImpl.class);

    public static final String JSON_RESPONSE_OK = "{'response':'ok'}";
    private static final String USER = "user";
    private static final String ACTIVE_DIRECTORY_DOMAIN = "active.directory.domain";
    private static final String ACTIVE_DIRECTORY_SERVER_NAME = "active.directory.server.name";
    private static final String ACTIVE_DIRECTORY_SERVER_PORT = "active.directory.server.port";

    @Override
    public Response login(HttpServletRequest request, String userName, String password) {
        final String domain = ApplicationProperties.get(ACTIVE_DIRECTORY_DOMAIN);
        final String host = ApplicationProperties.get(ACTIVE_DIRECTORY_SERVER_NAME);
        final String port = ApplicationProperties.get(ACTIVE_DIRECTORY_SERVER_PORT);

        final Hashtable<String, String> props = new Hashtable<>();
        props.put(javax.naming.Context.SECURITY_PRINCIPAL, userName + "@" + domain);
        props.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

        try {
            LdapCtxFactory.getLdapCtxInstance("ldap://" + host + ":" + port, props);
        } catch (NamingException e) {
            LOGGER.error("Unable to connect to Ldap",
                         e);
            // TODO: Check with Siemens's Health Care REST standards
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
}