package com.siemens.cto.aem.ws.rest.v1.impl;

import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.ws.rest.v1.service.HistoryServiceRest;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * {@link HistoryServiceRest} implementation.
 *
 * Created by JC043760 on 12/7/2015.
 */
public class HistoryServiceRestImpl implements HistoryServiceRest {

    private HistoryService historyService;

    public HistoryServiceRestImpl(final HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    public Response findHistory(final String groupName, final Integer numOfRec) {
        final List<JpaHistory> historyList = historyService.findHistory(groupName, null, numOfRec);
        return Response.ok(historyList).build();
    }

    @Override
    public Response findHistory(final String groupName, final String serverName, final Integer numOfRec) {
        final List<JpaHistory> historyList = historyService.findHistory(groupName, serverName, numOfRec);
        return Response.ok(historyList).build();
    }

}
