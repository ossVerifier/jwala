package com.cerner.jwala.ws.rest.v1.service.media.impl;

import com.cerner.jwala.persistence.jpa.domain.Media;
import com.cerner.jwala.service.MediaService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;

import com.cerner.jwala.ws.rest.v1.service.media.MediaServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

/**
 * Created by Rahul Sayini on 12/1/2016
 */
@Service
public class MediaServiceRestImpl implements MediaServiceRest {

    @Autowired
    private MediaService mediaService;

    @Override
    public Response createMedia(final Media media, final AuthenticatedUser aUser) {
        return ResponseBuilder.created(mediaService.create(media));
    }

    @Override
    public Response updateMedia(final String aMediaToUpdate, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok("Updated Media");
    }

    @Override
    public Response removeMedia(final String name, final AuthenticatedUser aUser) {
        mediaService.remove(name);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getAllMedia() {
        return ResponseBuilder.ok(mediaService.findAll());
    }

    @Override
    public Response getMedia(final String name, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok(mediaService.find(name));
    }

}
