package com.siemens.cto.aem.ws.rest;

import com.cerner.cto.ctp.ws.rest.response.ResponseContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Handler for internal server errors in the REST layer
 *
 * Created by JC043760 on 8/11/2016.
 */
public class RestServiceErrorHandler implements ExceptionMapper {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestServiceErrorHandler.class);
    public static final String INTERNAL_SERVER_ERR_MSG = "An error occurred while processing the request! Please check logs for details.";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

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

        return Response.status(status).header(CONTENT_TYPE, APPLICATION_JSON).entity(new ResponseContent() {

            public int getMsgCode() {
                return status;
            }

            public String getMsg() {
                return msg;
            }

        }).build();
    }
}
