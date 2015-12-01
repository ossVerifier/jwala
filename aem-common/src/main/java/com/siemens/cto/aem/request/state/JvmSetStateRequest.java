package com.siemens.cto.aem.request.state;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.jvm.JvmIdRule;
import com.siemens.cto.aem.rule.jvm.JvmStateRule;
import com.siemens.cto.aem.domain.model.state.CurrentState;

public class JvmSetStateRequest extends SetStateRequest<Jvm, JvmState> {

    private static final long serialVersionUID = 1L;

    public JvmSetStateRequest(final CurrentState<Jvm, JvmState> theNewState) {
        super(theNewState);
    }

    @Override
    public void validate() throws BadRequestException {
        final CurrentState<Jvm, JvmState> newState = getNewState();
        new MultipleRules(new JvmIdRule(newState.getId()),
                          new JvmStateRule(newState.getState())).validate();
    }
}
