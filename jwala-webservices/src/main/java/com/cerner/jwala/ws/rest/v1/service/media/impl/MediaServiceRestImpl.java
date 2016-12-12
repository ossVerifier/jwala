package com.cerner.jwala.ws.rest.v1.service.media.impl;

import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.service.MediaService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.media.MediaServiceRest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

/**
 * Implements {@link MediaServiceRest}
 */
@Service
public class MediaServiceRestImpl implements MediaServiceRest {

    @Autowired
    private MediaService mediaService;

    @Override
    public Response createMedia(final JpaMedia media, final AuthenticatedUser aUser) {
        return ResponseBuilder.created(mediaService.create(media));
    }

    @Override
    public Response updateMedia(final JpaMedia media, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok(mediaService.update(media));
    }

    @Override
    public Response removeMedia(final String name, final AuthenticatedUser aUser) {
        mediaService.remove(name);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getMedia(final Long id, final String mediaName, final AuthenticatedUser aUser) {
        if (id == null && StringUtils.isEmpty(mediaName)) {
            return ResponseBuilder.ok(mediaService.findAll());
        } else if (id != null) {
            return ResponseBuilder.ok(mediaService.find(id));
        }
        return ResponseBuilder.ok(mediaService.find(mediaName));
    }

    @Override
    public Response getMediaTypes() {
        return ResponseBuilder.ok(mediaService.getMediaTypes());
    }

}
