package com.siemens.cto.aem.service.state;

import java.util.Set;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;

public interface StateNotificationConsumer<T> {

    void close();

    boolean isClosed();

    void addNotification(final T aNotification);

    boolean isStale();

    Set<T> getNotifications(final TimeRemainingCalculator someTime);
}
