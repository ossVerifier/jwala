package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;

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
    void completeState(ControlJvmRequest controlJvmRequest, User aUser, String msg);
}