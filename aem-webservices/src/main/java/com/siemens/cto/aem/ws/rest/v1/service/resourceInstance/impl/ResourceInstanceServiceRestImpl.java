package com.siemens.cto.aem.ws.rest.v1.service.resourceInstance.impl;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonCreateJvm;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonUpdateJvm;
import com.siemens.cto.aem.ws.rest.v1.service.resourceInstance.ResourceInstanceServiceRest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by z003e5zv on 3/16/2015.
 */
public class ResourceInstanceServiceRestImpl implements ResourceInstanceServiceRest {

    private final ResourceService resourceService;
    private final GroupService groupService;
    private final JvmService jvmService;

    public ResourceInstanceServiceRestImpl(ResourceService resourceService, GroupService groupService, JvmService jvmService) {
        this.resourceService = resourceService;
        this.groupService = groupService;
        this.jvmService = jvmService;
    }
    @Override
    public Response createResourceInstance(JsonCreateResourceInstance aResourceInstanceToCreate, @BeanParam AuthenticatedUser aUser) {
        String jvmId = aResourceInstanceToCreate.getJvmId();
        String groupId = aResourceInstanceToCreate.getGroupId();
        if (jvmId != null && !"".equals(jvmId)) {
            // Note that if not found there will be well handled runtime exception detailing that it was not found
            jvmService.getJvm(new Identifier<Jvm>(jvmId));
        }
        else if (groupId != null && !"".equals(groupId)) {
            // Note that if not found there will be well handled runtime exception detailing that it was not found
            groupService.getGroup(new Identifier<Group>(groupId));
        }
        else {
            System.out.println("avoiding empty if statement");
        }
        return null;
    }

    @Override
    public Response updateResourceInstance(JsonUpdateResourceInstance aResourceInstanceToUpdate, @BeanParam AuthenticatedUser aUser) {
        return null;
    }

    @Override
    public Response removeResourceInstance(Identifier<ResourceInstance> aResourceInstanceId) {
        return null;
    }
}
