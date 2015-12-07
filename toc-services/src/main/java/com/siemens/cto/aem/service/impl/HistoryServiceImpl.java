package com.siemens.cto.aem.service.impl;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.service.HistoryService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link HistoryService} implementation.
 *
 * Created by JC043760 on 12/2/2015.
 */
public class HistoryServiceImpl implements HistoryService {

    private static final long DEFAULT_NUM_OF_REC = 30;
    public static final String HISTORY_INITIAL_LOAD_NUM_OF_REC = "history.initial-load-num-of-rec";
    private final HistoryDao historyDao;

    @Autowired
    public HistoryServiceImpl(final HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    @Override
    @Transactional
    public void write(final String name, final List<JpaGroup> groups, final String event, String user) {
        if (groups != null) {
            for (JpaGroup group : groups) {
                historyDao.write(name, group, event, user);
            }
        }
    }

    @Override
    public List<JpaHistory> read(String groupName) {
        return historyDao.read(groupName, NumberUtils.toLong(ApplicationProperties.get(HISTORY_INITIAL_LOAD_NUM_OF_REC),
                                                             DEFAULT_NUM_OF_REC));
    }

}
