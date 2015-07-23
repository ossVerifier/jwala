package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.state.CurrentState;

import java.util.List;

public interface StateNotificationConsumer {

    void close();

    boolean isClosed();

    void addNotification(final CurrentState<?, ?> aNotification);

    boolean isStale();

    /**
     * Return states sent within a duration
     * @param someTime
     */
    List<CurrentState<?, ?>> getNotifications(final TimeRemainingCalculator someTime);
    
    /**
     * Return a state update 
     * Uses the default polling duration, likely 30s
     * @return null or a state update.
     */
    CurrentState<?, ?> blockingGetNotification();
}
