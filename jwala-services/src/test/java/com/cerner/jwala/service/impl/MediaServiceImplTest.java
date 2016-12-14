package com.cerner.jwala.service.impl;

import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Created by RS045609 on 12/13/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class MediaServiceImplTest {
    @Mock
    private MediaDao mediaDao;

    private MediaServiceImpl mediaService;
    private JpaMedia media;
    private List<JpaMedia> list = createMediaList();

    private List<JpaMedia> createMediaList() {
        List<JpaMedia> mediaList = new ArrayList<>();
        media = new JpaMedia();
        media.setId(1L);
        media.setName("jdk 1.8");
        media.setType(MediaType.JDK);
        media.setLocalPath(Paths.get("c:/java/jdk.zip"));
        media.setRemoteDir(Paths.get("c:/ctp"));
        media.setMediaDir(Paths.get("jdk-1.8"));
        mediaList.add(media);
        return mediaList;
    }


    @Before
    public void setUp() {
        media = new JpaMedia();
        media.setId(1L);
        media.setName("jdk 1.8");
        media.setType(MediaType.JDK);
        media.setLocalPath(Paths.get("c:/java/jdk.zip"));
        media.setRemoteDir(Paths.get("c:/ctp"));
        media.setMediaDir(Paths.get("jdk-1.8"));
        when(mediaDao.findById(anyLong())).thenReturn(media);
        mediaService = new MediaServiceImpl();
        mediaService.setMediaDao(mediaDao);

    }

    @Test
    public void testFindById() {
        when(mediaDao.findById(anyLong())).thenReturn(media);
        JpaMedia result = mediaService.find(1L);
        assertEquals(result.getName(), media.getName());
    }

    @Test
    public void testFindByName() {
        when(mediaDao.find(anyString())).thenReturn(media);
        JpaMedia result = mediaService.find("jdk 1.8");
        assertEquals(result.getName(), media.getName());
    }

    @Test
    @Ignore
    // TODO: fix me!
    public void testCreate() {
        // when(mediaDao.create(any(JpaMedia.class))).thenReturn(media);
        // JpaMedia result = mediaService.create(media);
        // assertEquals(result.getName(), media.getName());
    }

    @Test
    public void testUpdate() {
        when(mediaDao.update(any(JpaMedia.class))).thenReturn(media);
        JpaMedia result = mediaService.update(media);
        assertEquals(result.getName(), media.getName());
    }

    @Test
    public void testFindAll() {
        when(mediaDao.findAll()).thenReturn(list);
        List<JpaMedia> result = mediaService.findAll();
        assertEquals(result.get(0).getName(), media.getName());
    }

    @Test
    public void testGetMediaTypes() {
        assertEquals(MediaType.values().length, mediaService.getMediaTypes().length);
    }

}
