package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;

/**
 * Gateway into the integration engine for state updates
 *
 */
public interface StateNotificationGateway {

    public void jvmStateChanged(CurrentJvmState jvmState);
    public void webServerStateChanged(Object webServerState);
}
