package com.siemens.cto.aem.service.jvm.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.Test;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessageKey;
import com.siemens.cto.aem.service.jvm.state.jms.sender.message.JvmStateUpdatedMessageExtractor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JvmStateUpdatedMessageExtractorTest {

    @Test
    public void testExtractId() throws Exception {
        final Identifier<Jvm> expectedId = new Identifier<>(123456L);
        final MapMessage message = mock(MapMessage.class);

        when(message.getString(eq(JvmStateMessageKey.JVM_ID.getKey()))).thenReturn(expectedId.getId().toString());

        final JvmStateUpdatedMessageExtractor extractor = new JvmStateUpdatedMessageExtractor(message);
        final Identifier<Jvm> actualId = extractor.extractId();

        assertEquals(expectedId,
                     actualId);
    }

    @Test(expected = JMSException.class)
    public void testBadExtraction() throws Exception {
        final Message message = mock(TextMessage.class);
        final JvmStateUpdatedMessageExtractor extractor = new JvmStateUpdatedMessageExtractor(message);
    }
}
