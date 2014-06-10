package com.siemens.cto.aem.service.jvm.state.jms.listener.message;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;

import static org.junit.Assert.assertEquals;

public class JvmStateMessageTest {

    @Test
    public void test() throws Exception {
        final CurrentJvmState expectedState = new CurrentJvmState(new Identifier<Jvm>("123456"),
                                                                  JvmState.STARTED,
                                                                  DateTime.now());
        final SetJvmStateCommand expectedCommand = new SetJvmStateCommand(expectedState);
        final String expectedId = expectedState.getJvmId().getId().toString();
        final JvmStateMessage message = new JvmStateMessage(expectedId,
                                                            expectedId,
                                                            "unused type",
                                                            expectedState.getJvmState().name(),
                                                            ISODateTimeFormat.dateTime().print(expectedState.getAsOf()));

        final SetJvmStateCommand actualCommand = message.toCommand();

        assertEquals(expectedCommand,
                     actualCommand);
    }
}
