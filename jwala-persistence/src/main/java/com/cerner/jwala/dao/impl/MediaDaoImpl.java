package com.cerner.jwala.dao.impl;

import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.Media;
import com.cerner.jwala.persistence.jpa.service.impl.AbstractCrudServiceImpl;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

/**
 * DAO that handles persistence operations with {@link com.cerner.jwala.persistence.jpa.domain.Media}
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
@Repository
public class MediaDaoImpl extends AbstractCrudServiceImpl<Media> implements MediaDao {

    @Override
    public Media find(final String name) {
        final Query q = entityManager.createNamedQuery(Media.QUERY_FIND_BY_NAME, Media.class);
        q.setParameter(Media.PARAM_NAME, name);
        return (Media) q.getSingleResult();
    }

}
