package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.resource.ResourceServiceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Created by z003e5zv on 3/16/2015.
 */
public class ResourceServiceRestImpl implements ResourceServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServiceRestImpl.class);

    private final ResourceService resourceService;
    private final JvmService jvmService;
    private final GroupService groupService;

    public ResourceServiceRestImpl(ResourceService resourceService, GroupService groupService, JvmService jvmService) {
        this.resourceService = resourceService;
        this.groupService = groupService;
        this.jvmService = jvmService;
    }

    @Override
    public Response getAll() {
        LOGGER.debug("Get All Resource Types requested." );
        return ResponseBuilder.ok(resourceService.getResourceTypes());
    }

    @Override
    public Response findResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, @MatrixParam("resourceTypeName")String resourceTypeName, final PaginationParamProvider paginationParamProvider) {
        if (name != null && !"".equals(name)) {
            return ResponseBuilder.ok(resourceService.getResourceInstanceByGroupNameAndName(groupName, name));
        }
        else if (resourceTypeName != null && !"".equals(resourceTypeName)) {
            return ResponseBuilder.ok(resourceService.getResourceInstancesByGroupNameAndResourceTypeName(groupName, resourceTypeName));
        }
        return ResponseBuilder.ok(resourceService.getResourceInstancesByGroupName(groupName));
    }

    @Override
    public Response createResourceInstance(JsonCreateResourceInstance aResourceInstanceToCreate, @BeanParam AuthenticatedUser aUser) {
        String groupId = aResourceInstanceToCreate.getGroupId();
        groupService.getGroup(new Identifier<Group>(groupId));
        return ResponseBuilder.ok(this.resourceService.createResourceInstance(aResourceInstanceToCreate.getCommand(), aUser.getUser()));
    }

    @Override
    public Response updateResourceInstanceAttributes(JsonUpdateResourceInstanceAttributes aResourceInstanceToUpdate, @BeanParam AuthenticatedUser aUser) {
        return ResponseBuilder.ok(this.resourceService.updateResourceInstanceAttributes(aResourceInstanceToUpdate.getComand(), aUser.getUser()));
    }
    @Override
    public Response updateResourceInstanceFriendlyName(JsonUpdateResourceInstanceFriendlyName jsonUpdateResourceInstanceFriendlyName, @BeanParam AuthenticatedUser aUser) {
        return ResponseBuilder.ok(this.resourceService.updateResourceInstanceFriendlyName(jsonUpdateResourceInstanceFriendlyName.parseCommand(), aUser.getUser()));
    }

    @Override
    public Response removeResourceInstance(Identifier<ResourceInstance> aResourceInstanceId) {
        this.resourceService.deleteResourceInstance(aResourceInstanceId);
        return ResponseBuilder.ok();
    }
}
