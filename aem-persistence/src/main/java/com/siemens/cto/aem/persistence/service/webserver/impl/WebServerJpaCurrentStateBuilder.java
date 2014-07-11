package com.siemens.cto.aem.persistence.service.webserver.impl;

import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
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
    public CurrentState<WebServer, WebServerReachableState> build() {
        return new CurrentState<>(createId(),
                                  createState(),
                                  createAsOf());
    }

    private WebServerReachableState createState() {
        return WebServerReachableState.convertFrom(currentState.getState());
    }
}
