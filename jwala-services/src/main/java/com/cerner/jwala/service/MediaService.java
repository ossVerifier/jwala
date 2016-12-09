package com.cerner.jwala.service;

import com.cerner.jwala.persistence.jpa.domain.Media;
import com.cerner.jwala.persistence.jpa.type.MediaType;

import java.util.List;

/**
 * Created by Jedd Cuison on 12/7/2016
 */
public interface MediaService {

    /**
     * Find a media
     * @param name name of the media
     * @return the {@link Media}
     */
    Media find(String name);

    /**
     * Find all media
     * @return List of {@link Media}
     */
    List<Media> findAll();

    /**
     * Create a media
     * @param media the media to create
     */
    Media create(Media media);

    /**
     * Remove media
     * @param name the name if the media to remove
     */
    void remove(String name);

    /**
     * Returns a list of {@link MediaType}
     * @return List of {@link MediaType}
     */
    MediaType [] getMediaTypes();


}
