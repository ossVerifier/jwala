package com.siemens.cto.aem.service.state.jms.sender.message;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class WebServerCurrentStateMessageExtractorTest extends AbstractCurrentStateMessageExtractorTest {

    private WebServerCurrentStateMessageExtractor extractor;

    @Before
    public void setup() throws Exception {
        extractor = new WebServerCurrentStateMessageExtractor();
    }

    @Test
    public void testExtractWithoutMessage() throws Exception {
        final CurrentState<WebServer, WebServerReachableState> expectedState = new CurrentState<>(new Identifier<WebServer>(123456L),
                                                                                                  WebServerReachableState.WS_REACHABLE,
                                                                                                  DateTime.now(),
                                                                                                  StateType.WEB_SERVER);

        setupMockMapMessage(expectedState);

        final CurrentState actualState = extractor.extract(message);
        assertEquals(expectedState,
                     actualState);
    }

    @Test
    public void testExtractWithMessage() throws Exception {
        final CurrentState<WebServer, WebServerReachableState> expectedState = new CurrentState<>(new Identifier<WebServer>(123456L),
                                                                                                  WebServerReachableState.WS_REACHABLE,
                                                                                                  DateTime.now(),
                                                                                                  StateType.WEB_SERVER,
                                                                                                  "This is the state message");

        setupMockMapMessage(expectedState);

        final CurrentState actualState = extractor.extract(message);
        assertEquals(expectedState,
                     actualState);
    }

}
