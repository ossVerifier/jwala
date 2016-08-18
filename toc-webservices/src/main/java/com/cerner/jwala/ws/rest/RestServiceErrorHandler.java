package com.cerner.jwala.ws.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Handler for internal server errors in the REST layer
 *
 * Note: This handler's purpose is to intercept uncaught errors so that they won't bubble up to the UI.
 *       It does not replace proper error handling in the REST layer.
 *
 * Created by JC043760 on 8/11/2016.
 */
public class RestServiceErrorHandler implements ExceptionMapper {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestServiceErrorHandler.class);
    public static final String INTERNAL_SERVER_ERR_MSG = "An error occurred while processing the request! Please check logs for details.";

    @Override
    public Response toResponse(final Throwable t) {
        LOGGER.error(t.getMessage(), t);

        final int status;
        final String msg;
        if (t instanceof WebApplicationException) {
            status = ((WebApplicationException) t).getResponse().getStatus();
            msg = t.getMessage();
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            msg = INTERNAL_SERVER_ERR_MSG;
        }

        return new JsonResponseBuilder().setStatusCode(status).setMessage(msg).build();
    }
}
