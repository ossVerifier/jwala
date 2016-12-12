package com.cerner.jwala.ws.rest.v1.service.media;

import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The REST service contract
 */
@Path("/media")
@Produces(MediaType.APPLICATION_JSON)
public interface MediaServiceRest {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createMedia(JpaMedia media, @BeanParam AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateMedia(JpaMedia media, @BeanParam AuthenticatedUser aUser);

    @DELETE
    @Path("/{mediaName}")
    Response removeMedia(@PathParam("mediaName") String name, @BeanParam AuthenticatedUser aUser);

    @GET
    Response getMedia(@MatrixParam("id") Long id, @MatrixParam("name") String mediaName, @BeanParam AuthenticatedUser aUser);

    @GET
    @Path("/types")
    Response getMediaTypes();

}
