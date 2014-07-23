package com.siemens.cto.aem.persistence.service.state.impl;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;

public abstract class AbstractJpaCurrentStateBuilder<S, T extends ExternalizableState> {

    private static final Chronology USE_DEFAULT_CHRONOLOGY = null;

    protected JpaCurrentState currentState;

    public AbstractJpaCurrentStateBuilder(final JpaCurrentState aCurrentState) {
        currentState = aCurrentState;
    }

    public abstract CurrentState<S, T> build();

    protected Identifier<S> createId() {
        return new Identifier<>(currentState.getId().getId());
    }

    protected DateTime createAsOf() {
        return new DateTime(currentState.getAsOf(),
                            USE_DEFAULT_CHRONOLOGY);
    }
}
