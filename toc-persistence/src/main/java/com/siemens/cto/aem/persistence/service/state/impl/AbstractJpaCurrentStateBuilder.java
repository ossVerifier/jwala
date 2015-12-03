package com.siemens.cto.aem.persistence.service.state.impl;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import org.joda.time.Chronology;
import org.joda.time.DateTime;

public abstract class AbstractJpaCurrentStateBuilder<S, T extends OperationalState> {

    private static final Chronology USE_DEFAULT_CHRONOLOGY = null;

    protected JpaCurrentState currentState;
    protected T staleStateOption;

    public AbstractJpaCurrentStateBuilder(final JpaCurrentState aCurrentState) {
        currentState = aCurrentState;
    }

    public CurrentState<S, T> build() {
        if (currentState != null) {
            if (hasMessage()) {
                return buildWithMessage();
            } else {
                return buildWithoutMessage();
            }
        }

        return null;
    }

    protected abstract CurrentState<S, T> buildWithMessage();

    protected abstract CurrentState<S, T> buildWithoutMessage();

    protected boolean hasMessage() {
        return currentState.getMessage() != null;
    }

    protected Identifier<S> createId() {
        return new Identifier<>(currentState.getId().getId());
    }

    protected DateTime createAsOf() {
        return new DateTime(currentState.getAsOf(),
                            USE_DEFAULT_CHRONOLOGY);
    }
    
    public AbstractJpaCurrentStateBuilder<S, T> setStaleOption(T staleState) { 
        staleStateOption = staleState;
        return this;
    }

}
