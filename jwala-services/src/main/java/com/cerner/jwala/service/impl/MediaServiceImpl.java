package com.cerner.jwala.service.impl;

import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import com.cerner.jwala.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements {@link MediaService}
 * <p/>
 * Created by Jedd Cuison on 12/7/2016
 */
@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaDao mediaDao;

    @Override
    public JpaMedia find(final Long id) {
        return mediaDao.findById(id);
    }

    @Override
    @Transactional
    public JpaMedia find(final String name) {
        return mediaDao.find(name);
    }

    @Override
    @Transactional
    public List<JpaMedia> findAll() {
        return mediaDao.findAll();
    }

    @Override
    @Transactional
    public JpaMedia create(final JpaMedia media) {
        return mediaDao.create(media);
    }

    @Override
    @Transactional
    public void remove(final String name) {
        mediaDao.remove(mediaDao.find(name));
    }

    @Override
    public MediaType[] getMediaTypes() {
        return MediaType.values();
    }

    @Override
    @Transactional
    public JpaMedia update(final JpaMedia media) {
        return mediaDao.update(media);
    }

    public void setMediaDao(MediaDao mediaDao) {
        this.mediaDao = mediaDao;
    }

}
