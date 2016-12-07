package com.cerner.jwala.ws.rest.v1.service.impl;

import com.cerner.jwala.service.MediaService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.MediaServiceRest;
import com.cerner.jwala.ws.rest.v1.service.media.impl.JsonCreateMedia;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

/**
 * Created by Rahul Sayini on 12/1/2016
 */
@Service
public class MediaServiceRestImpl implements MediaServiceRest {

    private final MediaService mediaService;

    public MediaServiceRestImpl(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public Response createMedia(final JsonCreateMedia jsonCreateMedia, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok("Created Media");
    }

    @Override
    public Response updateMedia(final String aMediaToUpdate, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok("Updated Media");
    }

    @Override
    public Response removeMedia(final Integer aMediaId, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok();
    }

    @Override
    public Response getAllMedia() {
        return ResponseBuilder.ok(mediaService.findAll());
    }

    @Override
    public Response getMedia(final String name, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok(mediaService.find(name));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}
