package com.cerner.jwala.common.request.state;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;
import com.cerner.jwala.common.rule.jvm.JvmStateRule;

public class JvmSetStateRequest extends SetStateRequest<Jvm, JvmState> {

    private static final long serialVersionUID = 1L;

    public JvmSetStateRequest(final CurrentState<Jvm, JvmState> theNewState) {
        super(theNewState);
    }

    @Override
    public void validate() {
        final CurrentState<Jvm, JvmState> newState = getNewState();
        new MultipleRules(new JvmIdRule(newState.getId()),
                          new JvmStateRule(newState.getState())).validate();
    }
}
