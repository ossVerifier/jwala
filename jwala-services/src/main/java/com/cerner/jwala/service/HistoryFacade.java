package com.cerner.jwala.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.type.EventType;

import java.util.Arrays;
import java.util.List;

/**
 * Brings history and messaging specific functionality together
 *
 * Created by JC043760 on 11/2/2016
 */
public class HistoryFacade {

    public static final String SUBJECT_HISTORY = "HISTORY";
    private final HistoryService historyService;
    private final MessagingService messagingService;

    public HistoryFacade(final HistoryService historyService, final MessagingService messagingService) {
        this.historyService = historyService;
        this.messagingService = messagingService;
    }

    public void write(final String serverName, final List<Group> groups, final String event, final EventType eventType,
                      final String user) {
        final List<JpaHistory> jpaHistoryList = historyService.createHistory(serverName, groups, event, eventType, user);
        for (JpaHistory jpaHistory : jpaHistoryList) {
            messagingService.send(new Message<>(jpaHistory.getGroup().getName(), SUBJECT_HISTORY, jpaHistory));
        }
    }

    public void write(final String serverName, final Group group, final String event, final EventType eventType,
                      final String user) {
        final List<Group> groupList = Arrays.asList(new Group[]{group});
        write(serverName, groupList, event, eventType, user);
    }
}
