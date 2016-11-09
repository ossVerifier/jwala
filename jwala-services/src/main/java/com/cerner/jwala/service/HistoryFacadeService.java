package com.cerner.jwala.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.type.EventType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Brings history and messaging functionalities together
 *
 * Created by JC043760 on 11/2/2016
 */
public class HistoryFacadeService {

    public static final String SUBJECT_HISTORY = "HISTORY";
    private final HistoryService historyService;
    private final MessagingService messagingService;

    public HistoryFacadeService(final HistoryService historyService, final MessagingService messagingService) {
        this.historyService = historyService;
        this.messagingService = messagingService;
    }

    /**
     * Write history and send it to the web socket
     *
     * @param serverName the server name
     * @param groups the groups where the event happened
     * @param event the event + the details of the event e.g. DEPLOY deploy context.xml resource
     * @param eventType {@link EventType}
     * @param user the user
     *
     * TODO: Split event into event name and description/details in the future
     */
    public void write(final String serverName, final Collection<Group> groups, final String event, final EventType eventType,
                      final String user) {
        final List<JpaHistory> jpaHistoryList = historyService.createHistory(serverName, new ArrayList<>(groups), event, eventType, user);
        for (JpaHistory jpaHistory : jpaHistoryList) {
            messagingService.send(new Message<>(jpaHistory.getGroup().getName(), SUBJECT_HISTORY, jpaHistory));
        }
    }

    /**
     * Write history and send it to the web socket
     *
     * @param serverName the server name
     * @param group the group where the event happened
     * @param event the event + the details of the event e.g. DEPLOY deploy context.xml resource
     * @param eventType {@link EventType}
     * @param user the user
     */
    public void write(final String serverName, final Group group, final String event, final EventType eventType,
                      final String user) {
        final List<Group> groupList = Arrays.asList(new Group[]{group});
        write(serverName, groupList, event, eventType, user);
    }
}
