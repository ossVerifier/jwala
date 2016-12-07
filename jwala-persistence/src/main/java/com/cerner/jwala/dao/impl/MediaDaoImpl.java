package com.cerner.jwala.dao.impl;

import com.cerner.jwala.persistence.jpa.domain.Media;
import com.cerner.jwala.dao.MediaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

/**
 * DAO that handles persistence operations with {@link com.cerner.jwala.persistence.jpa.domain.Media}
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
@Repository
public class MediaDaoImpl extends AbstractDao<Media> implements MediaDao {

    @Autowired
    public MediaDaoImpl(final EntityManager em) {
        super(em);
    }

}
