package com.cerner.jwala.service.impl;

import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.Media;
import com.cerner.jwala.service.MediaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements {@link MediaService}
 *
 * Created by JC043760 on 12/7/2016.
 */
@Service
public class MediaServiceImpl implements MediaService {

    private final MediaDao mediaDao;

    public MediaServiceImpl(final MediaDao mediaDao) {
        this.mediaDao = mediaDao;
    }

    @Override
    @Transactional
    public Media find(final String name) {
        return mediaDao.find(name);
    }

    @Override
    @Transactional
    public List<Media> findAll() {
        return mediaDao.findAll();
    }

    @Override
    @Transactional
    public void create(final Media media) {
        mediaDao.create(media);
    }

    @Override
    @Transactional
    public void remove(final String name) {
        mediaDao.remove(mediaDao.find(name));
    }

}
