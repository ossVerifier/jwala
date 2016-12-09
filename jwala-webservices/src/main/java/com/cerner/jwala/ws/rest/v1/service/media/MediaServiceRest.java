package com.cerner.jwala.ws.rest.v1.service.media;

import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Rahul Sayini on 12/1/2016
 */

@Path("/media")
@Produces(MediaType.APPLICATION_JSON)
public interface MediaServiceRest {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createMedia(JsonMedia jsonCreateMedia, @BeanParam AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateMedia(String aJvmToUpdate, @BeanParam AuthenticatedUser aUser);

    @DELETE
    @Path("/{mediaId}")
    Response removeMedia(@PathParam("mediaId") String name, @BeanParam AuthenticatedUser aUser);

    @GET
    Response getAllMedia();

    @GET
    @Path("/{aMediaName}")
    Response getMedia(@PathParam("aMediaName") String aMediaName, @BeanParam AuthenticatedUser aUser);

}
