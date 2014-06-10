package com.siemens.cto.aem.service.jvm.state.jms.sender.message;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessageKey;
import com.siemens.cto.aem.service.jvm.state.jms.sender.message.JvmStateUpdatedMessageCreator;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmStateUpdatedMessageCreatorTest {

    private JvmStateUpdatedMessageCreator creator;
    private Identifier<Jvm> jvmId;
    private Session session;
    private MapMessage mapMessage;

    @Before
    public void setUp() throws Exception {
        session = mock(Session.class);
        mapMessage = mock(MapMessage.class);
        when(session.createMapMessage()).thenReturn(mapMessage);
        jvmId = new Identifier<>(123456L);
        creator = new JvmStateUpdatedMessageCreator(jvmId);
    }

    @Test
    public void testCreateMessage() throws Exception {

        final Message actualMessage = creator.createMessage(session);

        verify(mapMessage, times(1)).setString(eq(JvmStateMessageKey.JVM_ID.getKey()),
                                               eq(jvmId.getId().toString()));
        assertSame(mapMessage,
                   actualMessage);
    }
}
