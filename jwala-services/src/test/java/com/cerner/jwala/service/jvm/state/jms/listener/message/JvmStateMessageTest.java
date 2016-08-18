package com.cerner.jwala.service.jvm.state.jms.listener.message;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.jvm.message.JvmStateMessage;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.request.state.JvmSetStateRequest;
import com.cerner.jwala.common.request.state.SetStateRequest;

import static org.junit.Assert.assertEquals;

public class JvmStateMessageTest {

    @Test
    public void test() throws Exception {
        final CurrentState<Jvm, JvmState> expectedState = new CurrentState<>(new Identifier<Jvm>("123456"),
                                                                             JvmState.JVM_STARTED,
                                                                             DateTime.now(),
                                                                             StateType.JVM);
        final JvmSetStateRequest expectedCommand = new JvmSetStateRequest(expectedState);
        final String expectedId = expectedState.getId().getId().toString();
        final JvmStateMessage message = new JvmStateMessage(expectedId,
                                                            expectedId,
                                                            "unused type",
                                                            expectedState.getState().toPersistentString(),
                                                            ISODateTimeFormat.dateTime().print(expectedState.getAsOf()));

        final SetStateRequest<Jvm, JvmState> actualCommand = message.toCommand();

        assertEquals(expectedCommand,
                     actualCommand);
    }
}
