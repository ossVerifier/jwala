package com.cerner.jwala.common.domain.model.app;

import com.cerner.jwala.common.domain.model.state.OperationalState;

public enum ApplicationState implements OperationalState {
    FAILED;

    @Override
    public String toStateLabel() {
        return null;
    }

    @Override
    public String toPersistentString() {
        return null;
    }
}
