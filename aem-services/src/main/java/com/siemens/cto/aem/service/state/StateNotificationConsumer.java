package com.siemens.cto.aem.service.state;

import java.util.List;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;

public interface StateNotificationConsumer<T> {

    void close();

    boolean isClosed();

    void addNotification(final T aNotification);

    boolean isStale();

    List<T> getNotifications(final TimeRemainingCalculator someTime);
}
