package com.siemens.cto.aem.persistence.dao.impl;

import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * {@link HistoryDao} implementation.
 *
 * Created by JC043760 on 11/30/2015.
 */
public class HistoryDaoImpl implements HistoryDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager em;

    @Override
    public void write(final String name, final JpaGroup group, final String event, String user) {
        em.persist(new JpaHistory(name, group, event, user));
    }

}
