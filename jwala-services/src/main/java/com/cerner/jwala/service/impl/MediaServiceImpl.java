package com.cerner.jwala.service.impl;

import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import com.cerner.jwala.service.MediaService;
import com.cerner.jwala.service.resource.ResourceRepositoryService;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link MediaService}
 * <p/>
 * Created by Jedd Cuison on 12/7/2016
 */
@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaDao mediaDao;

    @Autowired
    private ResourceRepositoryService resourceRepositoryService;

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
    public JpaMedia create(final Map<String, String> mediaDataMap, final Map<String, Object> mediaFileDataMap) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JpaMedia media = objectMapper.convertValue(mediaDataMap, JpaMedia.class);

        final String filename = (String) mediaFileDataMap.get("filename");
        media.setMediaDir(Paths.get(filename.substring(0, filename.lastIndexOf("."))));

        final String dest = resourceRepositoryService.upload(filename, (BufferedInputStream) mediaFileDataMap.get("content"));

        media.setLocalPath(Paths.get(dest));

        return mediaDao.create(media);
    }

    @Override
    @Transactional
    public void remove(final String name) {
        final JpaMedia jpaMedia = mediaDao.find(name);
        resourceRepositoryService.delete(jpaMedia.getLocalPath().getFileName().toString());
        mediaDao.remove(jpaMedia);
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
