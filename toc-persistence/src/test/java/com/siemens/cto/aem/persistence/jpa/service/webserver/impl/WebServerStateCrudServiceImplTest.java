package com.siemens.cto.aem.persistence.jpa.service.webserver.impl;

import com.siemens.cto.aem.request.state.SetStateRequest;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.request.state.WebServerSetStateRequest;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {WebServerStateCrudServiceImplTest.Config.class
                      })
public class WebServerStateCrudServiceImplTest {

    @Autowired
    private WebServerStateCrudServiceImpl impl;

    private User user;

    @Before
    public void setup() throws Exception {
        user = new User("unused");
    }

    @Test
    public void testUpdateState() throws Exception {
        final Identifier<WebServer> expectedId = new Identifier<>(123456L);
        final WebServerReachableState expectedState = WebServerReachableState.WS_REACHABLE;
        final DateTime expectedAsOf = DateTime.now();

        final JpaCurrentState state = updateState(expectedId,
                                                  expectedState,
                                                  expectedAsOf);

        assertEquals(expectedId.getId(),
                     state.getId().getId());
        assertEquals(StateType.WEB_SERVER,
                     state.getId().getStateType());
        assertEquals(expectedState.toPersistentString(),
                     state.getState());
    }

    @Test
    public void testGetState() throws Exception {
        final Identifier<WebServer> expectedId = new Identifier<>(123456L);
        final WebServerReachableState expectedState = WebServerReachableState.WS_UNREACHABLE;
        final DateTime expectedAsOf = DateTime.now();

        updateState(expectedId,
                    expectedState,
                    expectedAsOf);

        final JpaCurrentState actualState = impl.getState(expectedId);

        assertEquals(expectedId.getId(),
                     actualState.getId().getId());
        assertEquals(StateType.WEB_SERVER,
                     actualState.getId().getStateType());
        assertEquals(expectedState.toPersistentString(),
                     actualState.getState());
    }

    @Test
    public void testGetStates() throws Exception {

        final int numberToCreate = 5;
        final Map<Identifier<WebServer>, WebServerReachableState> expectedData = createData(numberToCreate,
                                                                                        WebServerReachableState.WS_REACHABLE);
        for (final Map.Entry<Identifier<WebServer>, WebServerReachableState> data : expectedData.entrySet()) {
            updateState(data.getKey(),
                        data.getValue(),
                        DateTime.now());
        }

        final List<JpaCurrentState> states = impl.getStates();
        for (final JpaCurrentState state : states) {
            final Identifier<WebServer> actualId = new Identifier<>(state.getId().getId());
            assertTrue(expectedData.containsKey(actualId));
            assertEquals(expectedData.get(actualId).toPersistentString(),
                         state.getState());
        }
    }

    private Map<Identifier<WebServer>, WebServerReachableState> createData(final int aNumberToCreate,
                                                                           final WebServerReachableState aState) {
        final Map<Identifier<WebServer>, WebServerReachableState> ids = new HashMap<>(aNumberToCreate);
        for (int i = 0; i < aNumberToCreate; i++) {
            ids.put(new Identifier<WebServer>((long) (i + 1)),
                    aState);
        }
        return ids;
    }

    private JpaCurrentState updateState(final Identifier<WebServer> anId,
                                        final WebServerReachableState aState,
                                        final DateTime anAsOf) {
        return impl.updateState(new Event<SetStateRequest<WebServer, WebServerReachableState>>(new WebServerSetStateRequest(new CurrentState<>(anId,
                                                                                                                                               aState,
                                                                                                                                               anAsOf,
                                                                                                                                               StateType.WEB_SERVER)),
                                                                                               AuditEvent.now(user)));
    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public WebServerStateCrudServiceImpl getWebServerStateCrudServiceImpl() {
            return new WebServerStateCrudServiceImpl();
        }
    }
}
