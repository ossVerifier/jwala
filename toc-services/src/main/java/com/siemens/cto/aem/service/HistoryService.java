package com.siemens.cto.aem.service;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.type.EventType;

import java.util.List;

/**
 * History service.
 *
 * Created by JC043760 on 12/2/2015.
 */
public interface HistoryService {

    /**
     * Create history data.
     * @param serverName the server name
     * @param groups list of {@link JpaGroup}
     * @param event the event
     * @param eventType @{link EventType}
     * @param user the user name/id
     */
    void createHistory(String serverName, List<JpaGroup> groups, String event, EventType eventType, String user);

    /**
     * Retrieve history data.
     * @param groupName the group name
     * @param numOfRec The Number of records to fetch. If null, all records are retrieved.
     * @return a list of {@link JpaHistory}
     */
    List<JpaHistory> findHistory(String groupName, Integer numOfRec);

}
