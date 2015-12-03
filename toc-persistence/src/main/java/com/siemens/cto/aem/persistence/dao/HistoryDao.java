package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

/**
 * History DAO.
 *
 * Created by JC043760 on 11/30/2015.
 */
public interface HistoryDao {

    void write(JpaGroup group, String event);

}
