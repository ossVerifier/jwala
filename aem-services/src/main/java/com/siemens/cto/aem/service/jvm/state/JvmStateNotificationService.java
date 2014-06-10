package com.siemens.cto.aem.service.jvm.state;

import java.util.Set;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public interface JvmStateNotificationService {

    JvmStateNotificationConsumerId register();

    void deregister(final JvmStateNotificationConsumerId aConsumerId);

    boolean isValid(final JvmStateNotificationConsumerId aConsumerId);

    void notifyJvmStateUpdated(final Identifier<Jvm> aJvmId);

    Set<Identifier<Jvm>> pollUpdatedStates(final JvmStateNotificationConsumerId aConsumerId,
                                           final TimeRemainingCalculator aTimeRemaining);
}
