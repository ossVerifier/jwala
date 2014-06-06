package com.siemens.cto.aem.ws.rest.v1.service.user.impl;

import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.user.UserServiceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * Created by z002xuvs on 5/29/2014.
 */
public class UserServiceRestImpl implements UserServiceRest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceRestImpl.class);
    public static final String JSON_RESPONSE_OK = "{'response':'ok'}";

    @Override
    public Response logout(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Entered logout for user: {}", request.getUserPrincipal());
        request.getSession().invalidate();
        return ResponseBuilder.ok(JSON_RESPONSE_OK);
    }
}
