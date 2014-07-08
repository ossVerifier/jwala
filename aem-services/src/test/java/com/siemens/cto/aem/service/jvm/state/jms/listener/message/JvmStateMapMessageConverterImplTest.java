package com.siemens.cto.aem.service.jvm.state.jms.listener.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.infrastructure.report.runnable.jms.impl.ReportingJmsMessageKey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JvmStateMapMessageConverterImplTest {

    private JvmStateMapMessageConverter converter;
    private MapMessage source;

    @Before
    public void setUp() throws Exception {
        source = mock(MapMessage.class);
        converter = new JvmStateMapMessageConverterImpl();
    }

    @Test
    public void testTo() throws Exception {
        final JvmStateMessage expectedMessage = new JvmStateMessage("The Id",
                                                                      "The Instance Id",
                                                                      "The Type",
                                                                      "The State",
                                                                      "The As Of Date Time");

        mockStringProperty(ReportingJmsMessageKey.ID, expectedMessage.getId());
        mockStringProperty(ReportingJmsMessageKey.INSTANCE_ID, expectedMessage.getInstanceId());
        mockStringProperty(ReportingJmsMessageKey.TYPE, expectedMessage.getType());
        mockStringProperty(ReportingJmsMessageKey.STATE, expectedMessage.getState());
        mockStringProperty(ReportingJmsMessageKey.AS_OF, expectedMessage.getAsOf());

        final JvmStateMessage actualMessage = converter.convert(source);

        assertEquals(expectedMessage,
                     actualMessage);
    }

    protected void mockStringProperty(final ReportingJmsMessageKey aKey,
                                      final String aValue) throws JMSException {
        when(source.getString(Matchers.eq(aKey.getKey()))).thenReturn(aValue);
    }
}
