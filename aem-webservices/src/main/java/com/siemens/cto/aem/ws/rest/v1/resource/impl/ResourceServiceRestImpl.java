package com.siemens.cto.aem.ws.rest.v1.resource.impl;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.resource.ResourceServiceRest;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;

@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public class ResourceServiceRestImpl implements ResourceServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServiceRestImpl.class);

    private final ResourceService service;

    public ResourceServiceRestImpl(final ResourceService resourceService) {
        service = resourceService;
    }

    @Override
    public Response getAll() { 
        LOGGER.debug("Get All Resource Types requested." );
        return ResponseBuilder.ok(service.getResourceTypes());
    }

}
