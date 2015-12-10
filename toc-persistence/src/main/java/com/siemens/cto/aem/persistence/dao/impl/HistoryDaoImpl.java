package com.siemens.cto.aem.persistence.dao.impl;

import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.type.EventType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * {@link HistoryDao} implementation.
 *
 * Created by JC043760 on 11/30/2015.
 */
public class HistoryDaoImpl implements HistoryDao {

    private static final String PARAM_GROUP_NAME = "groupName";

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager em;

    @Override
    public void createHistory(final String serverName, final JpaGroup group, final String event, EventType eventType,
                              final String user) {
        em.persist(new JpaHistory(serverName, group, event, eventType, user));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaHistory> findHistory(final String groupName, final Integer numOfRec) {
        final Query q = em.createNamedQuery(JpaHistory.QRY_GET_HISTORY_BY_GROUP_NAME);
        q.setParameter(PARAM_GROUP_NAME, groupName);
        if (numOfRec != null) {
            q.setMaxResults(numOfRec);
        }
        return q.getResultList();
    }

}
