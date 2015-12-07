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

    void createHistory(String name, JpaGroup group, String event, String user);

    List<JpaHistory> findHistory(String groupName, long numOfRecs);

}