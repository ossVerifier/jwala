package com.siemens.cto.aem.service.jvm.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * {@link JvmStateServiceImpl} tests.
 *
 * Created by JC043760 on 5/12/2016.
 */
public class JvmStateServiceImplTest {
    private JvmStateService jvmStateService;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> mockInMemoryStateManagerService;

    @Mock
    private JvmStateResolverWorker mockJvmStateResolverWorker;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private GroupStateNotificationService mockGroupStateNotificationService;

    private static final long JVM_STATE_UPDATE_INTERVAL = 60000;

    @Mock
    private RemoteCommandExecutorService mockRemoteCommandExecutorService;

    @Mock
    private SshConfiguration mockSshConfig;

    private static final  long LOCK_TIMEOUT = 600000;

    private static final  int KEY_LOCK_STRIPE_COUNT = 120;

    @Before
    public void setup() {
        initMocks(this);
        jvmStateService = new JvmStateServiceImpl(mockJvmPersistenceService,
                                                  mockInMemoryStateManagerService,
                                                  mockJvmStateResolverWorker,
                                                  mockMessagingService,
                                                  mockGroupStateNotificationService,
                                                  JVM_STATE_UPDATE_INTERVAL,
                                                  mockRemoteCommandExecutorService,
                                                  mockSshConfig,
                                                  LOCK_TIMEOUT,
                                                  KEY_LOCK_STRIPE_COUNT);
    }

    @Test
    public void testVerifyAndUpdateNotInMemOrStaleStates() {
        final List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(new Jvm(new Identifier<Jvm>(1L), "some-jvm", new HashSet<Group>()));
        when(mockJvmPersistenceService.getJvms()).thenReturn(jvmList);
        jvmStateService.verifyAndUpdateNotInMemOrStaleStates();
        verify(mockJvmStateResolverWorker).pingAndUpdateJvmState(eq(jvmList.get(0)), any(JvmStateService.class));
    }

    @Test
    public void testUpdateNotInMemOrStaleState() {

    }

    @Test
    public void testGetServiceStatus() {

    }

    @Test
    public void testUpdateState() {

    }

    @Test
    public void testUpdateState2() {

    }

    @Test
    public void testRequestCurrentStatesRetrievalAndNotification() {

    }
}