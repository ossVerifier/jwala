package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface JvmControlServiceLifecycle {

    JvmControlHistory startHistory(final ControlJvmCommand aCommand,
                                   final User aUser);

    /**
     * Set state, return previous state.
     */
    CurrentState<Jvm, JvmState> startState(final ControlJvmCommand aCommand,
                    final User aUser);

    /**
     * Set state to previous state; no message.
     */
    void revertState(CurrentState<Jvm, JvmState> aJvmState,
                        final User aUser);

    void startStateWithMessage(final Identifier<Jvm> aJvmId,
                               final JvmState aJvmState,
                               final String aMessage,
                               final User aUser);

    JvmControlHistory completeHistory(final JvmControlHistory incompleteHistory,
                                      final ControlJvmCommand aCommand,
                                      final ExecData execData,
                                      final User aUser);

    void notifyMessageOnly(Identifier<Jvm> jvmId, String result, User aUser);
}