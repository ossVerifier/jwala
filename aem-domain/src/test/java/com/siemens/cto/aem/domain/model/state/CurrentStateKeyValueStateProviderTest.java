package com.siemens.cto.aem.domain.model.state;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.domain.model.state.message.StateKey;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CurrentStateKeyValueStateProviderTest {

    @Mock
    private KeyValueStateConsumer consumer;

    @Test
    public void testProvideStateWithoutMessage() throws Exception {
        final Identifier<WebServer> id = new Identifier<>(123456L);
        final WebServerReachableState state = WebServerReachableState.WS_REACHABLE;
        final DateTime asOf = DateTime.now();
        final StateType type = StateType.WEB_SERVER;

        final CurrentState<WebServer, WebServerReachableState> producer = new CurrentState<>(id,
                                                                                             state,
                                                                                             asOf,
                                                                                             type);
        producer.provideState(consumer);

        verifyKeySet(CommonStateKey.ID,
                     id.getId().toString());
        verifyKeySet(CommonStateKey.STATE,
                     state.toStateString());
        verifyKeySet(CommonStateKey.AS_OF,
                     ISODateTimeFormat.dateTime().print(asOf));
        verifyKeySet(CommonStateKey.TYPE,
                     type.name());
        verifyKeySet(CommonStateKey.MESSAGE,
                     "");
    }

    @Test
    public void testProvideStateWithMessage() throws Exception {
        final Identifier<WebServer> id = new Identifier<>(123456L);
        final WebServerReachableState state = WebServerReachableState.WS_REACHABLE;
        final DateTime asOf = DateTime.now();
        final StateType type = StateType.WEB_SERVER;
        final String message = "This is the state message";

        final CurrentState<WebServer, WebServerReachableState> producer = new CurrentState<>(id,
                                                                                             state,
                                                                                             asOf,
                                                                                             type,
                                                                                             message);
        producer.provideState(consumer);

        verifyKeySet(CommonStateKey.ID,
                     id.getId().toString());
        verifyKeySet(CommonStateKey.STATE,
                     state.toStateString());
        verifyKeySet(CommonStateKey.AS_OF,
                     ISODateTimeFormat.dateTime().print(asOf));
        verifyKeySet(CommonStateKey.TYPE,
                     type.name());
        verifyKeySet(CommonStateKey.MESSAGE,
                     message);
    }

    private void verifyKeySet(final StateKey aKey,
                              final String aValue) {
        verify(consumer, times(1)).set(eq(aKey),
                                       eq(aValue));
    }
}
