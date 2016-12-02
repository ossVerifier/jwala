package com.cerner.jwala.ws.rest.v1.service.media;

import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import org.springframework.beans.factory.InitializingBean;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by RS045609 on 12/1/2016.
 */

@Path("/media")
@Produces(MediaType.APPLICATION_JSON)
public interface MediaServiceRest extends InitializingBean {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createMedia(final String jsonCreateMedia,
                         @BeanParam final AuthenticatedUser aUser);


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateMedia(final String aJvmToUpdate,
                         @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{mediaId}")
    Response removeMedia(@PathParam("mediaId") final Integer aMediaId,
                         @BeanParam final AuthenticatedUser aUser);

    @GET
    Response getMedia();

}
