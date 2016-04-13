package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.resource.EntityType;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.resource.ResourceServiceRest;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;

/**
 * {@link ResourceServiceRest} implementation.
 *
 * Created by z003e5zv on 3/16/2015.
 */
public class ResourceServiceRestImpl implements ResourceServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServiceRestImpl.class);
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String FILENAME = "filename";
    private static final int CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS = 2;

    private final ResourceService resourceService;

    public ResourceServiceRestImpl(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public Response getTypes() {
        LOGGER.debug("Get All Resource Types requested." );
        return ResponseBuilder.ok(resourceService.getResourceTypes());
    }

    @Override
    public Response findResourceInstanceByGroup(String groupName) {
        return ResponseBuilder.ok(resourceService.getResourceInstancesByGroupName(groupName));
    }

    @Override
    public Response generateResourceInstanceByNameGroup(String name, String groupName) {
        return ResponseBuilder.ok(resourceService.generateResourceInstanceFragment(groupName, name));
    }
    @Override
    public Response findResourceInstanceByNameGroup(final String name, final String groupName) {
        return ResponseBuilder.ok(resourceService.getResourceInstancesByGroupName(groupName));
    }

    @Override
    public Response createResourceInstance(JsonResourceInstance aResourceInstanceToCreate, AuthenticatedUser aUser) {
        return ResponseBuilder.ok(this.resourceService.createResourceInstance(aResourceInstanceToCreate.getCommand(), aUser.getUser()));
    }

    @Override
    public Response updateResourceInstanceAttributes(final String name, final String groupName, JsonResourceInstance aResourceInstanceToUpdate, AuthenticatedUser aUser) {
        return ResponseBuilder.ok(this.resourceService.updateResourceInstance(groupName, name, aResourceInstanceToUpdate.getCommand(), aUser.getUser()));
    }

    @Override
    public Response removeResourceInstance( final String name, final String groupName) {
        this.resourceService.deleteResourceInstance(groupName, name);
        return ResponseBuilder.ok();
    }

    @Override
    public Response removeResources(String groupName, List<String> resourceNames) {
        try {
            resourceService.deleteResources(groupName, resourceNames);
            return ResponseBuilder.ok();
        } catch (RuntimeException e) {
            LOGGER.error("Could not remove resources {}", resourceNames);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response getTemplate(final String resourceTypeName) {
        return ResponseBuilder.ok(resourceService.getTemplate(resourceTypeName));
    }

    @Override
    // TODO: Refactor resourceService.createTemplate(inputStreams[0], inputStreams[1]) since it looks ambiguous.
    public Response createTemplate(final List<Attachment> attachments, final AuthenticatedUser user) {
        try {
            if (attachments.size() == CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS) {
                final InputStream [] inputStreams = new InputStream[CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS];
                for (int i = 0; i < attachments.size(); i++) {
                    final DataHandler handler = attachments.get(i).getDataHandler();
                    try {
                        inputStreams[i] = handler.getInputStream();
                    } catch (final IOException ioe) {
                        return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                new FaultCodeException(AemFaultType.IO_EXCEPTION, ioe.getMessage()));
                    }
                }
                return ResponseBuilder.created(resourceService.createTemplate(inputStreams[0], inputStreams[1]));
            } else {
                return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.INVALID_NUMBER_OF_ATTACHMENTS,
                        "Invalid number of attachments! 2 attachments is expected by the service."));
            }
        } catch (final ResourceServiceException rse) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.SERVICE_EXCEPTION, rse.getMessage()));
        }
    }

    @Override
    public Response removeTemplate(final String name) {
        try {
            return ResponseBuilder.ok(resourceService.removeTemplate(name));
        } catch (final ResourceServiceException rse) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.PERSISTENCE_ERROR, rse.getMessage()));
        }
    }

    @Override
    public Response removeTemplate(@PathParam("groupName") final String groupName, @PathParam("entityType") final EntityType entityType,
                                   @QueryParam("templateNames") final String templateNames) {
        try {
            return ResponseBuilder.ok(resourceService.removeTemplate(groupName, entityType, templateNames));
        } catch (final ResourceServiceException rse) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.PERSISTENCE_ERROR, rse.getMessage()));
        }
    }

    @Override
    public Response removeTemplate(@PathParam("entityType") final EntityType entityType, @PathParam("entityName") final String entityName,
                                   @QueryParam("templateNames") final String templateNames) {
        try {
            return ResponseBuilder.ok(resourceService.removeTemplate(entityType, entityName, templateNames));
        } catch (final ResourceServiceException rse) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.PERSISTENCE_ERROR, rse.getMessage()));
        }
    }
}
