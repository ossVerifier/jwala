package com.siemens.cto.aem.persistence.service.builder;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;

public class JvmJpaCurrentStateBuilder extends AbstractJpaCurrentStateBuilder<Jvm, JvmState> {

    public JvmJpaCurrentStateBuilder(final JpaCurrentState aCurrentState) {
        super(aCurrentState);
    }

    public JvmJpaCurrentStateBuilder setCurrentState(final JpaCurrentState aCurrentState) {
        currentState = aCurrentState;
        return this;
    }

    @Override
    protected CurrentState<Jvm, JvmState> buildWithMessage() {
        return new CurrentState<>(createId(),
                                  createState(),
                                  createAsOf(),
                                  StateType.JVM,
                                  currentState.getMessage());
    }

    @Override
    protected CurrentState<Jvm, JvmState> buildWithoutMessage() {
        return new CurrentState<>(createId(),
                                  createState(),
                                  createAsOf(),
                                  StateType.JVM);
    }

    private JvmState createState() {
        if(staleStateOption != null) {
            return staleStateOption;
        } else {
            return JvmState.convertFrom(currentState.getState());
        }
    }
}
