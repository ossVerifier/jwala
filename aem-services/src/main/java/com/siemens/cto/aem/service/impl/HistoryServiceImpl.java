package com.siemens.cto.aem.service.impl;

import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link HistoryService} implementation.
 *
 * Created by JC043760 on 12/2/2015.
 */
class HistoryServiceImpl implements HistoryService {

    private final HistoryDao historyDao;

    @Autowired
    public HistoryServiceImpl(final HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    @Override
    @Transactional
    public void write(final JpaGroup group, final String event) {
        historyDao.write(group, event);
    }

}
