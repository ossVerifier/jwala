package com.cerner.jwala.dao;

import com.cerner.jwala.persistence.jpa.domain.Media;

import java.util.List;

/**
 * Persistence layer that deals with media
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
public interface MediaDao {

    Media findById(Long id);

    Media find(String name);

    List<Media> findAll();

    Media create(Media media);

    void remove(Media media);

    Media update(Media media);
}
