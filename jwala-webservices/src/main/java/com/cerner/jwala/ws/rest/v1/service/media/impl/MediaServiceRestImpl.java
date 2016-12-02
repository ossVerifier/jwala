package com.cerner.jwala.ws.rest.v1.service.media.impl;

import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.media.MediaServiceRest;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RS045609 on 12/1/2016.
 */
public class MediaServiceRestImpl implements MediaServiceRest {


    @Override
    public Response createMedia(final String jsonCreateMedia, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok("Created Media");
    }

    @Override
    public Response updateMedia(final String aMediaToUpdate, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok("Updated Media");
    }

    @Override
    public Response removeMedia(final Integer aMediaId, final AuthenticatedUser aUser) {
        return ResponseBuilder.ok("removed JDK 1.7");
    }

    @Override
    public Response getMedia() {
        Media media = new Media(1, "mediaName", "mediaPath", "mediaType", "mediaRemoteHostPath");
        List<Media> mediaList = new ArrayList<Media>();
        mediaList.add(media);
        return ResponseBuilder.ok(mediaList);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
