package com.siemens.cto.aem.ws.rest.v1.service.jvm.state.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumerId;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;

public class JvmStateConsumerManager {

    protected static final String STATE_NOTIFICATION_CONSUMER_SESSION_KEY = "jvmStateConsumer";

    private final JvmStateNotificationService notificationService;

    public JvmStateConsumerManager(final JvmStateNotificationService theJvmNotificationService) {
        notificationService = theJvmNotificationService;
    }

    public JvmStateNotificationConsumerId getConsumerId(final HttpServletRequest aRequest,
                                                        final String aClientId) {
        final HttpSession session = aRequest.getSession();
        final JvmStateNotificationConsumerId consumerId = getOrConstructConsumerId(session,
                                                                                   aClientId);
        return consumerId;
    }

    private JvmStateNotificationConsumerId getOrConstructConsumerId(final HttpSession aSession,
                                                                    final String aClientId) {
        final String sessionKey = createSessionKey(aClientId);
        JvmStateNotificationConsumerId consumerId = getConsumerFromSession(aSession,
                                                                           sessionKey);
        if (isInvalid(consumerId)) {
            consumerId = constructNewConsumerId();
            setConsumerIntoSession(aSession,
                                   sessionKey,
                                   consumerId);
        }

        return consumerId;
    }

    private JvmStateNotificationConsumerId constructNewConsumerId() {
        return notificationService.register();
    }

    private JvmStateNotificationConsumerId getConsumerFromSession(final HttpSession aSession,
                                                                  final String aSessionKey) {
        final JvmStateNotificationConsumerId consumerId = (JvmStateNotificationConsumerId)aSession.getAttribute(aSessionKey);
        return consumerId;
    }

    private void setConsumerIntoSession(final HttpSession aSession,
                                        final String aSessionKey,
                                        final JvmStateNotificationConsumerId aConsumerId) {
        aSession.setAttribute(aSessionKey, aConsumerId);
    }

    private boolean isInvalid(final JvmStateNotificationConsumerId aConsumerId) {
        return (aConsumerId == null) || (!notificationService.isValid(aConsumerId));
    }

    String createSessionKey(final String aClientId) {
        return STATE_NOTIFICATION_CONSUMER_SESSION_KEY + "." + aClientId;
    }
}
