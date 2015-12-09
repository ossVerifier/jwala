package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;

import java.util.List;

/**
 * History DAO.
 *
 * Created by JC043760 on 11/30/2015.
 */
public interface HistoryDao {

    /**
     * Create history data.
     * @param serverName the server name
     * @param group {@link JpaGroup}
     * @param event the event
     * @param user the user name/id
     */
    void createHistory(String serverName, JpaGroup group, String event, String user);

    /**
     * Retrieve history data.
     * @param groupName the group name
     * @param numOfRec The Number of records to fetch. If null, all records are retrieved.
     * @return a list of {@link JpaHistory}
     */
    List<JpaHistory> findHistory(String groupName, Integer numOfRec);

}