package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public interface JvmStateNotificationService {

    void notifyJvmStateUpdated(final Identifier<Jvm> aJvmId);
}
