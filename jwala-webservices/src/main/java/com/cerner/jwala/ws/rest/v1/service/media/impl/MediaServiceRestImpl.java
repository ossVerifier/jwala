package com.cerner.jwala.ws.rest.v1.service.media.impl;

import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.media.MediaServiceRest;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

/**
 * Created by RS045609 on 12/1/2016.
 */
public class MediaServiceRestImpl implements MediaServiceRest {


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
        List<Media> mediaList = Arrays.asList(
                new Media(1, "Windows JDK 1.7", "C:/jdk1.7.zip", "JDK", "D:/stp/jdk1.7"),
                new Media(2, "Windows Apache Tomcat 7.0.55", "C:/apache-tomcat-7.0.55.zip", "Tomcat", "D:/stp/apache-tomcat-7.0.55"),
                new Media(3, "jdk1.8.0_92.zip", "D:/stp/toc-1.3.80/apache-tomcat-7.0.55/data/binaries", "JDK", "D:/stp"),
                new Media(4, "Windows Apache Tomcat 8.0.20", "C:/apache-tomcat-8.0.20.zip", "Tomcat", "D:/stp/apache-tomcat-8.0.20"));
        return ResponseBuilder.ok(mediaList);
    }

    @Override
    public Response getMedia(final String aMediaName, final AuthenticatedUser aUser) {
        Media media = new Media(1, "mediaName", "mediaPath", "mediaType", "mediaRemoteHostPath");
        return ResponseBuilder.ok(media);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
