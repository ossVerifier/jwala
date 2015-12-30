package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.History;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * {@link HistoryCrudService} implementation.
 *
 * Created by JC043760 on 11/30/2015.
 */
public class HistoryCrudServiceImpl extends AbstractCrudServiceImpl<JpaHistory, History> implements HistoryCrudService {

    private static final String PARAM_GROUP_NAME = "groupName";
    private static final String PARAM_SERVER_NAME = "serverName";

    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryCrudServiceImpl.class);


    @Override
    public void createHistory(final String serverName, final Group group, final String event, EventType eventType, final String user) {
        if (group == null) {
            LOGGER.warn("Attempting to insert a history row without an associated group.");
            return;
        }

        //TODO: Inject GroupCrudService to find the group
        JpaGroup jpaGroup = entityManager.find(JpaGroup.class, group.getId());
        create(new JpaHistory(serverName, jpaGroup, event, eventType, user));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaHistory> findHistory(final String groupName, final String serverName, final Integer numOfRec) {
        final Query q = entityManager.createNamedQuery(StringUtils.isEmpty(serverName) ? JpaHistory.QRY_GET_HISTORY_BY_GROUP_NAME :
                                            JpaHistory.QRY_GET_HISTORY_BY_GROUP_NAME_AND_SERVER_NAME);

        q.setParameter(PARAM_GROUP_NAME, groupName);

        if (!StringUtils.isEmpty(serverName)) {
            q.setParameter(PARAM_SERVER_NAME, serverName);
        }

        if (numOfRec != null) {
            q.setMaxResults(numOfRec);
        }

        return q.getResultList();
    }

}
