package com.cerner.jwala.service.state;

import java.util.List;

import javax.jms.JMSException;

import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.time.TimeRemainingCalculator;

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
