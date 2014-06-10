package com.siemens.cto.aem.service.jvm.state;

import java.util.Set;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;

public interface JvmStateNotificationConsumer {

    void close();

    boolean isClosed();

    void addNotification(final Identifier<Jvm> aJvmId);

    boolean isStale();

    Set<Identifier<Jvm>> getNotifications(final TimeRemainingCalculator someTime);
}
