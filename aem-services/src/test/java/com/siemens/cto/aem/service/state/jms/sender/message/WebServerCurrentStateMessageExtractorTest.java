package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.domain.model.state.message.StateKey;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebServerCurrentStateMessageExtractorTest {

    private WebServerCurrentStateMessageExtractor extractor;

    @Mock
    private MapMessage message;

    @Before
    public void setup() throws Exception {
        extractor = new WebServerCurrentStateMessageExtractor();
    }

    @Test
    public void testExtract() throws Exception {
        final CurrentState<WebServer, WebServerReachableState> expectedState = new CurrentState<>(new Identifier<WebServer>(123456L),
                                                                                                  WebServerReachableState.REACHABLE,
                                                                                                  DateTime.now(),
                                                                                                  StateType.WEB_SERVER);

        setupMockMapMessage(expectedState);

        final CurrentState actualState = extractor.extract(message);
        assertEquals(expectedState,
                     actualState);
    }

    private void setupMockMapMessage(final CurrentState<WebServer, WebServerReachableState> aState) throws JMSException {
        mockMapString(CommonStateKey.AS_OF, ISODateTimeFormat.dateTime().print(aState.getAsOf()));
        mockMapString(CommonStateKey.ID, aState.getId().getId().toString());
        mockMapString(CommonStateKey.STATE, aState.getState().toStateString());
        mockMapString(CommonStateKey.TYPE, aState.getType().name());
    }

    private void mockMapString(final StateKey aKey,
                               final String aValue) throws JMSException {
        when(message.getString(eq(aKey.getKey()))).thenReturn(aValue);
    }
}
