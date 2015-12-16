package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;

public interface JvmControlServiceLifecycle {

    /**
     * Set state, return previous state.
     */
    CurrentState<Jvm, JvmState> startState(final ControlJvmRequest controlJvmRequest,
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


    void notifyMessageOnly(Identifier<Jvm> jvmId, String result, User aUser);

    /* Set completed state to confirmed state, no message. */
    void completeState(ControlJvmRequest controlJvmRequest, User aUser);
}