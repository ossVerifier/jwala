package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.resource.ResourceServiceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * {@link ResourceServiceRest} implementation.
 *
 * Created by z003e5zv on 3/16/2015.
 */
public class ResourceServiceRestImpl implements ResourceServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServiceRestImpl.class);

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
    public Response createTemplate(final String metaDataFile, final String templateFile, final AuthenticatedUser user) {
        try {
            resourceService.createTemplate(metaDataFile, templateFile, user.getUser());
            return ResponseBuilder.ok();
        } catch (final RuntimeException rte) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.PERSISTENCE_ERROR, rte.getMessage()));
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
}
