package com.siemens.cto.aem.service.jvm.state.jms.listener.message;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.command.state.JvmSetStateCommand;
import com.siemens.cto.aem.domain.command.state.SetStateCommand;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JvmStateMessageTest {

    @Test
    public void test() throws Exception {
        final CurrentState<Jvm, JvmState> expectedState = new CurrentState<>(new Identifier<Jvm>("123456"),
                                                                             JvmState.JVM_STARTED,
                                                                             DateTime.now(),
                                                                             StateType.JVM);
        final JvmSetStateCommand expectedCommand = new JvmSetStateCommand(expectedState);
        final String expectedId = expectedState.getId().getId().toString();
        final JvmStateMessage message = new JvmStateMessage(expectedId,
                                                            expectedId,
                                                            "unused type",
                                                            expectedState.getState().toPersistentString(),
                                                            ISODateTimeFormat.dateTime().print(expectedState.getAsOf()));

        final SetStateCommand<Jvm, JvmState> actualCommand = message.toCommand();

        assertEquals(expectedCommand,
                     actualCommand);
    }
}
