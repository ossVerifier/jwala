package com.siemens.cto.aem.service.impl;

import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link HistoryService} implementation.
 *
 * Created by JC043760 on 12/2/2015.
 */
public class HistoryServiceImpl implements HistoryService {

    private final HistoryDao historyDao;

    @Autowired
    public HistoryServiceImpl(final HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    @Override
    @Transactional
    public void createHistory(final String serverName, final List<JpaGroup> groups, final String event, final String user) {
        if (groups != null) {
            for (JpaGroup group : groups) {
                historyDao.createHistory(serverName, group, event, user);
            }
        }
    }

    @Override
    public List<JpaHistory> findHistory(final String groupName, final Integer numOfRec) {
        return historyDao.findHistory(groupName, numOfRec);
    }

}
