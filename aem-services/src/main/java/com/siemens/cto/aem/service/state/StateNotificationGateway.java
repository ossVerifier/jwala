package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

/**
 * Gateway into the integration engine for state updates
 *
 */
public interface StateNotificationGateway {

    public void jvmStateChanged(CurrentState<Jvm, JvmState> jvmState);
    public void webServerStateChanged(CurrentState<WebServer, WebServerReachableState> webServerState);
    public void groupStateChanged(CurrentState<Group, GroupState> anUpdatedState);
}
