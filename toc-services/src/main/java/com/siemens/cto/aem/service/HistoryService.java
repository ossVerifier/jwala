package com.siemens.cto.aem.service;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import java.util.List;

/**
 * History service.
 *
 * Created by JC043760 on 12/2/2015.
 */
public interface HistoryService {

    void write(String name, List<JpaGroup> groups, String event, String user);

}
