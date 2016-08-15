package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.ws.rest.v2.service.ResponseContent;

import javax.ws.rs.core.Response;

/**
 * Builder for a {@link Response} that returns application/json content
 *
 * Created by JC043760 on 8/14/2016.
 */
public class JsonResponseBuilder<T> {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    private Response.Status status;
    private int statusCode;
    private String message;
    private T content;

    public JsonResponseBuilder setStatus(final Response.Status status) {
        this.status = status;
        return this;
    }

    public JsonResponseBuilder setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public JsonResponseBuilder setMessage(final String message) {
        this.message = message;
        return this;
    }

    public JsonResponseBuilder setResponseContent(final T content) {
        this.content = content;
        return this;
    }

    public JsonResponseBuilder setContent(T content) {
        this.content = content;
        return this;
    }

    public Response build() {
        int statusCode = status != null ? status.getStatusCode() : this.statusCode;
        return Response.status(statusCode)
                       .header(CONTENT_TYPE, APPLICATION_JSON)
                       .entity(new ResponseContent<>(statusCode, message, content)).build();
    }
}
