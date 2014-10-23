package com.siemens.cto.aem.service.state;

import java.util.List;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.state.CurrentState;

public interface StateNotificationConsumer {

    void close();

    boolean isClosed();

    void addNotification(final CurrentState<?, ?> aNotification);

    boolean isStale();

    List<CurrentState<?, ?>> getNotifications(final TimeRemainingCalculator someTime);
}
