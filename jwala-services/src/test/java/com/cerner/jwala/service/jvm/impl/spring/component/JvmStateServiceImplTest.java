package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.jvm.impl.spring.component.JvmStateResolverWorker;
import com.cerner.jwala.service.jvm.impl.spring.component.JvmStateServiceImpl;
import com.cerner.jwala.service.state.InMemoryStateManagerService;

import org.apache.commons.lang.StringUtils;
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
 * Created by Jedd Cuison on 5/12/2016.
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
        final Jvm jvm = new Jvm(new Identifier<Jvm>(1L), "some-jvm", new HashSet<Group>());
        jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
        verify(mockJvmPersistenceService).updateState(eq(jvm.getId()), eq(JvmState.JVM_STARTED), eq(StringUtils.EMPTY));
        verify(mockMessagingService).send(any(CurrentState.class));
        verify(mockGroupStateNotificationService).retrieveStateAndSendToATopic(eq(jvm.getId()), eq(Jvm.class));
    }

    @Test
    public void testGetServiceStatus() {
        final Jvm jvm = new Jvm(new Identifier<Jvm>(1L), "some-jvm", new HashSet<Group>());
        jvmStateService.getServiceStatus(jvm);
        verify(mockRemoteCommandExecutorService).executeCommand(any(RemoteExecCommand.class));
    }

    @Test
    public void testUpdateState() {
        final Identifier<Jvm> id = new Identifier<>(1L);
        jvmStateService.updateState(id, JvmState.JVM_STOPPED);
        verify(mockJvmPersistenceService).updateState(eq(id), eq(JvmState.JVM_STOPPED), eq(StringUtils.EMPTY));
        verify(mockMessagingService).send(any(CurrentState.class));
        verify(mockGroupStateNotificationService).retrieveStateAndSendToATopic(eq(id), eq(Jvm.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRequestCurrentStatesRetrievalAndNotification() {
        final List<JpaJvm> jpaJvmList = new ArrayList<>();
        jpaJvmList.add(new JpaJvm());
        when(mockJvmPersistenceService.getJpaJvmsByGroupName(eq("some-group-name"))).thenReturn(jpaJvmList);
        when(mockInMemoryStateManagerService.get(any(Identifier.class))).thenReturn(null);
        jvmStateService.requestCurrentStatesRetrievalAndNotification("some-group-name");
        verify(mockMessagingService).send(any(CurrentState.class));
    }
}