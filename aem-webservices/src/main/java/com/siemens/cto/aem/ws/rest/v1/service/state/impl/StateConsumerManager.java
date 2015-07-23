package com.siemens.cto.aem.ws.rest.v1.service.state.impl;

import com.siemens.cto.aem.service.state.StateNotificationConsumerId;
import com.siemens.cto.aem.service.state.StateNotificationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class StateConsumerManager {

    protected static final String STATE_NOTIFICATION_CONSUMER_SESSION_KEY = "stateConsumer";

    private final StateNotificationService notificationService;

    public StateConsumerManager(final StateNotificationService theNotificationService) {
        notificationService = theNotificationService;
    }

    public StateNotificationConsumerId getConsumerId(final HttpServletRequest aRequest,
                                                     final String aClientId) {
        final HttpSession session = aRequest.getSession();
        final StateNotificationConsumerId consumerId = getOrConstructConsumerId(session,
                                                                                aClientId);
        return consumerId;
    }

    private StateNotificationConsumerId getOrConstructConsumerId(final HttpSession aSession,
                                                                 final String aClientId) {
        final String sessionKey = createSessionKey(aClientId);
        StateNotificationConsumerId consumerId = getConsumerFromSession(aSession,
                                                                        sessionKey);
        if (isInvalid(consumerId)) {
            consumerId = constructNewConsumerId();
            setConsumerIntoSession(aSession,
                                   sessionKey,
                                   consumerId);
        }

        return consumerId;
    }

    private StateNotificationConsumerId constructNewConsumerId() {
        return notificationService.register();
    }

    private StateNotificationConsumerId getConsumerFromSession(final HttpSession aSession,
                                                                  final String aSessionKey) {
        final StateNotificationConsumerId consumerId = (StateNotificationConsumerId)aSession.getAttribute(aSessionKey);
        return consumerId;
    }

    private void setConsumerIntoSession(final HttpSession aSession,
                                        final String aSessionKey,
                                        final StateNotificationConsumerId aConsumerId) {
        aSession.setAttribute(aSessionKey, aConsumerId);
    }

    private boolean isInvalid(final StateNotificationConsumerId aConsumerId) {
        return (aConsumerId == null) || (!notificationService.isValid(aConsumerId));
    }

    String createSessionKey(final String aClientId) {
        return STATE_NOTIFICATION_CONSUMER_SESSION_KEY + "." + aClientId;
    }
}
