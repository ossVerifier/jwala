package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.AbstractStateServiceFacade;

public class JvmStateServiceFacade extends AbstractStateServiceFacade<Jvm, JvmState> {

    public JvmStateServiceFacade(final StateService<Jvm, JvmState> theService) {
        super(theService,
              StateType.JVM);
    }

    @Override
    protected SetStateCommand<Jvm, JvmState> createCommand(final CurrentState<Jvm, JvmState> aNewCurrentState) {
        return new JvmSetStateCommand(aNewCurrentState);
    }
}
