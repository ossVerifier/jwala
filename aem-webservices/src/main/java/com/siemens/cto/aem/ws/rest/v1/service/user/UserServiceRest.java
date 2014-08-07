package com.siemens.cto.aem.ws.rest.v1.service.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public interface UserServiceRest {

    @POST
    @Path("/login")
    Response login(@Context HttpServletRequest request,
                   @FormParam("userName") String userName,
                   @FormParam("password") String password);

    @POST
    @Path("/logout")
    Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response);

}