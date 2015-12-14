package com.siemens.cto.aem.service.state;

import java.util.List;

import javax.jms.JMSException;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;

public interface StateNotificationConsumer {

    void close();

    boolean isClosed();

    void addNotification(final CurrentState<?, ?> aNotification);

    boolean isStale();

    /**
     * Return states sent within a duration
     * 
     * @param someTime
     */
    List<CurrentState<?, ?>> getNotifications(final TimeRemainingCalculator someTime) throws JMSException;

    /**
     * Return a state update Uses the default polling duration, likely
     * 30s
     * 
     * @return null or a state update.
     */
    CurrentState<?, ?> blockingGetNotification() throws JMSException;
}
