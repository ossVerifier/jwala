package com.siemens.cto.aem.service.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link HistoryService} implementation.
 *
 * Created by JC043760 on 12/2/2015.
 */
public class HistoryServiceImpl implements HistoryService {

    private final HistoryCrudService historyCrudService;

    @Autowired
    public HistoryServiceImpl(final HistoryCrudService historyCrudService) {
        this.historyCrudService = historyCrudService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Write history independent of any transaction.
    public void createHistory(final String serverName, final List<Group> groups, final String event,
                              final EventType eventType, final String user) {
        if (groups != null) {
            for (Group group : groups) {
                historyCrudService.createHistory(serverName, group, event, eventType, user);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JpaHistory> findHistory(final String groupName, final String serverName, final Integer numOfRec) {
        return historyCrudService.findHistory(groupName, serverName, numOfRec);
    }

}
