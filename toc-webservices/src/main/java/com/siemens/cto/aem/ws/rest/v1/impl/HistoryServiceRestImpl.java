package com.siemens.cto.aem.ws.rest.v1.impl;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.ws.rest.v1.service.HistoryServiceRest;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by JC043760 on 12/7/2015.
 */
public class HistoryServiceRestImpl implements HistoryServiceRest {

    private HistoryService historyService;

    public HistoryServiceRestImpl(final HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    public Response read(final String groupName) {
        final List<JpaHistory> historyList = historyService.read(groupName);

        // Prevent circular relationships (history has groups and groups has history) to manifest in the JSON response.
        // TODO: Do above via JPA or when data is serialized to the UI (whichever is the best approach).
        for (JpaHistory history: historyList) {
            final JpaGroup group = new JpaGroup();
            group.setName(groupName);
            history.setGroup(group);
        }

        return Response.ok(historyList).build();
    }

}
