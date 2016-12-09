package com.cerner.jwala.ws.rest.v1.service.media;

import com.cerner.jwala.persistence.jpa.domain.Media;
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
    Response createMedia(Media media, @BeanParam AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateMedia(String aJvmToUpdate, @BeanParam AuthenticatedUser aUser);

    @DELETE
    @Path("/{mediaName}")
    Response removeMedia(@PathParam("mediaName") String name, @BeanParam AuthenticatedUser aUser);

    @GET
    Response getMedia(@MatrixParam("name") String aMediaName, @BeanParam AuthenticatedUser aUser);

    @GET
    @Path("/types")
    Response getMediaTypes();

}
