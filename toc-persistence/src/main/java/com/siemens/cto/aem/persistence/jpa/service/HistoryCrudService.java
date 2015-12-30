package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.History;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.type.EventType;

import java.util.List;

/**
 * History DAO.
 *
 * Created by JC043760 on 11/30/2015.
 */
public interface HistoryCrudService extends CrudService<JpaHistory> {

    /**
     * Create history data.
     * @param serverName the server name
     * @param group {@link JpaGroup}
     * @param event the event
     * @param eventType {@link EventType}
     * @param user the user name/id
     */
    void createHistory(String serverName, Group group, String event, EventType eventType, String user);

    /**
     * Retrieve history data.
     * @param groupName the group name
     * @param serverName the server name, if null the history of all the servers belonging to the group will be queried
     *@param numOfRec The Number of records to fetch. If null, all records are retrieved.  @return a list of {@link JpaHistory}
     */
    List<JpaHistory> findHistory(String groupName, String serverName, Integer numOfRec);

}