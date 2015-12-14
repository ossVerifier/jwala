package com.siemens.cto.aem.persistence.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.service.state.impl.AbstractJpaCurrentStateBuilder;

public class WebServerJpaCurrentStateBuilder extends AbstractJpaCurrentStateBuilder<WebServer, WebServerReachableState> {

    public WebServerJpaCurrentStateBuilder(final JpaCurrentState aCurrentState) {
        super(aCurrentState);
    }

    public WebServerJpaCurrentStateBuilder setCurrentState(final JpaCurrentState aCurrentState) {
        currentState = aCurrentState;
        return this;
    }

    @Override
    protected CurrentState<WebServer, WebServerReachableState> buildWithMessage() {
        return new CurrentState<>(createId(),
                                  createState(),
                                  createAsOf(),
                                  StateType.WEB_SERVER,
                                  currentState.getMessage());
    }

    @Override
    protected CurrentState<WebServer, WebServerReachableState> buildWithoutMessage() {
        return new CurrentState<>(createId(),
                                  createState(),
                                  createAsOf(),
                                  StateType.WEB_SERVER);
    }

    private WebServerReachableState createState() {
        if(staleStateOption != null) {
            return staleStateOption;
        } else {
            return WebServerReachableState.convertFrom(currentState.getState());
        }
    }
}
