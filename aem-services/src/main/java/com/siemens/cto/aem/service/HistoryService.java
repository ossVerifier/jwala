package com.siemens.cto.aem.service;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

/**
 * History service.
 *
 * Created by JC043760 on 12/2/2015.
 */
public interface HistoryService {

    void write(JpaGroup group, String event);

}
