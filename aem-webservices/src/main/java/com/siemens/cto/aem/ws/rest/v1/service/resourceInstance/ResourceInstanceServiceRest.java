package com.siemens.cto.aem.ws.rest.v1.service.resourceInstance;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonCreateJvm;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonUpdateJvm;
import com.siemens.cto.aem.ws.rest.v1.service.resourceInstance.impl.JsonCreateResourceInstance;
import com.siemens.cto.aem.ws.rest.v1.service.resourceInstance.impl.JsonUpdateResourceInstance;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@Path("resourceInstance")
public interface ResourceInstanceServiceRest {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createResourceInstance(final JsonCreateResourceInstance aResourceInstanceToCreate,
                       @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstance(final JsonUpdateResourceInstance aResourceInstanceToUpdate,
                       @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{resourceInstanceId}")
    Response removeResourceInstance(@PathParam("resourceInstanceId") final Identifier<ResourceInstance> aResourceInstanceId);

}
