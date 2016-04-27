package com.siemens.cto.aem.service.jvm.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Future;

/**
 * {@link JvmStateService} implementation.
 *
 * Created by JC043760 on 3/22/2016.
 */
@Service
public class JvmStateServiceImpl implements JvmStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateServiceImpl.class);

    private final JvmPersistenceService jvmPersistenceService;
    private final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService;
    private final JvmStateResolverWorker jvmStateResolverWorker;
    private final long jvmStateUpdateInterval;
    private final MessagingService messagingService;
    private final GroupStateNotificationService groupStateNotificationService;
    private final RemoteCommandExecutorService remoteCommandExecutorService;
    private final SshConfiguration sshConfig;

    private static final Map<Identifier<Jvm>, Future<CurrentState<Jvm, JvmState>>> PING_FUTURE_MAP = new HashMap<>();

    @Autowired
    public JvmStateServiceImpl(final JvmPersistenceService jvmPersistenceService,
                               @Qualifier("jvmInMemoryStateManagerService")
                               final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService,
                               final JvmStateResolverWorker jvmStateResolverWorker,
                               final MessagingService messagingService,
                               final GroupStateNotificationService groupStateNotificationService,
                               @Value("${jvm.state.update.interval:60000}")
                               final long jvmStateUpdateInterval,
                               final RemoteCommandExecutorService remoteCommandExecutorService,
                               final SshConfiguration sshConfig) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.inMemoryStateManagerService = inMemoryStateManagerService;
        this.jvmStateResolverWorker = jvmStateResolverWorker;
        this.jvmStateUpdateInterval = jvmStateUpdateInterval;
        this.messagingService = messagingService;
        this.groupStateNotificationService = groupStateNotificationService;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
        this.sshConfig = sshConfig;
    }

    @Override
    @Scheduled(fixedDelayString = "${ping.jvm.period.millis}")
    public void verifyAndUpdateNotInMemOrStartedAndStaleStates() {
        final List<Jvm> jvms = jvmPersistenceService.getJvms();
        for (final Jvm jvm : jvms) {
            if ((!isStateInMemory(jvm) || (isStarted(jvm) && isStale(jvm))) &&
                (!PING_FUTURE_MAP.containsKey(jvm.getId()) || PING_FUTURE_MAP.get(jvm.getId()).isDone())) {
                    LOGGER.debug("Pinging JVM {} ...", jvm.getJvmName());
                    PING_FUTURE_MAP.put(jvm.getId(), jvmStateResolverWorker.pingAndUpdateJvmState(jvm, this));
                    LOGGER.debug("Pinged JVM {}", jvm.getJvmName());
            }
        }
    }

    /**
     * Check if the JVM's state is stale by checking the state's time stamp.
     * @param jvm {@link Jvm}
     * @return true if the state is stale.
     */
    protected boolean isStale(final Jvm jvm) {
        final long interval = DateTime.now().getMillis() - inMemoryStateManagerService.get(jvm.getId()).getAsOf().getMillis();
        if (interval > jvmStateUpdateInterval) {
            LOGGER.debug("JVM {}'s state is stale. Interval since last update = {} sec!", jvm.getJvmName(), interval/1000);
            return true;
        }
        return false;
    }

    /**
     * Checks if a JVM's state is set to started.
     * @param jvm {@link Jvm}
     * @return true if the state is started.
     */
    protected boolean isStarted(final Jvm jvm) {
        return inMemoryStateManagerService.get(jvm.getId()).getState().equals(JvmState.JVM_STARTED);
    }

    @Override
    public void updateNotInMemOrStartedButStaleState(final Jvm jvm, final JvmState state, final String errMsg) {
        // Check again before updating to make sure that nothing has change after pinging the JVM.
        if (!isStateInMemory(jvm) || (isStarted(jvm) && isStale(jvm))) {
                LOGGER.debug("Updating state of JVM {} ...", jvm.getJvmName());
                updateState(jvm.getId(), state, errMsg);
                groupStateNotificationService.retrieveStateAndSendToATopic(jvm.getId(), Jvm.class);
                LOGGER.debug("Updated state of JVM {}!", jvm.getJvmName());
        }
    }

    /**
     * Check if the state in the application context state map
     * @param jvm {@link Jvm}
     * @return true if the state of a certain JVM is in the application context state map.
     */
    protected boolean isStateInMemory(Jvm jvm) {
        return inMemoryStateManagerService.containsKey(jvm.getId());
    }

    @Override
    public RemoteCommandReturnInfo getServiceStatus(final Jvm jvm) {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                sshConfig.getPassword(), jvm.getHostName(), sshConfig.getPort()) , new ExecCommand("sc query '" +
                jvm.getJvmName() + "' | grep STATE"));
        return remoteCommandExecutorService.executeCommand(remoteExecCommand);
    }

    @Override
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        updateState(id, state, StringUtils.EMPTY);
    }

    @Override
    public void updateState(final Identifier<Jvm> id, final JvmState state, final String errMsg) {
        LOGGER.debug("The state has changed from {} to {}", inMemoryStateManagerService.get(id) != null ?
                inMemoryStateManagerService.get(id).getState() : "NO STATE", state);
        // If the JVM is already stopped and the new state is stopping, don't do anything!
        // We can't go from stopped to stopping. The stopped state that the JvmControlService issued has
        // the last say for it means that the (windows) service has already stopped.
        if (!(JvmState.JVM_STOPPING.equals(state) && isCurrentStateStopped(id))) {
            if (isStateChangedAndOrMsgNotEmpty(id, state, errMsg)) {
                jvmPersistenceService.updateState(id, state, errMsg);
                messagingService.send(new CurrentState<>(id, state, DateTime.now(), StateType.JVM, errMsg));
                groupStateNotificationService.retrieveStateAndSendToATopic(id, Jvm.class);
            }
            // Always update the JVM state since JvmStateService.verifyAndUpdateNotInMemOrStartedAndStaleStates checks if the
            // state is stale of not!
            inMemoryStateManagerService.put(id, new CurrentState<>(id, state, DateTime.now(), StateType.JVM));
        } else {
            LOGGER.warn("Ignoring {} state since the JVM is currently stopped.", state);
        }
    }

    /**
     * Checks whether the current state of the JVM is stopped.
     * @return true if the state is stopped.
     */
    protected boolean isCurrentStateStopped(final Identifier<Jvm> id) {
        if (inMemoryStateManagerService.containsKey(id)) {
            return JvmState.JVM_STOPPED.equals(inMemoryStateManagerService.get(id).getState());
        }
        final Jvm jvm = jvmPersistenceService.getJvm(id);
        return JvmState.JVM_STOPPED.equals(jvm.getState());
    }

    /**
     * Check if the state has changed and-or message is not empty.
     * @param id {@link Identifier<Jvm>}
     * @param state the state
     * @param errMsg error message
     * @return returns true if the state is not the same compared to the previous state or if there's a message (error message)
     */
    protected boolean isStateChangedAndOrMsgNotEmpty(final Identifier<Jvm> id, final JvmState state, final String errMsg) {
        final boolean newOrStateChanged = !inMemoryStateManagerService.containsKey(id) ||
                !inMemoryStateManagerService.get(id).getState().equals(state);

        final boolean newOrMsgChanged = StringUtils.isNotEmpty(errMsg) &&
                (!inMemoryStateManagerService.containsKey(id)  ||
                        !inMemoryStateManagerService.get(id).getMessage().equals(errMsg));

        return newOrStateChanged || newOrMsgChanged;
    }

    /**
     * This method will deliver the states using a messaging service.
     *
     * @param groupName the group name
     * @return number of JVM state notifications.
     */
    @Override
    public int requestCurrentStatesRetrievalAndNotification(final String groupName) {
        final List<JpaJvm> jpaJvmList = jvmPersistenceService.getJvmsByGroupId(groupName);
            for (final JpaJvm jpaJvm : jpaJvmList) {
                final CurrentState<Jvm, JvmState> inMemoryState = inMemoryStateManagerService.get(new Identifier<Jvm>(jpaJvm.getId()));
                if (inMemoryState == null) {
                    LOGGER.debug("in-memory JVM state = null");
                    final CurrentState<Jvm, JvmState> currentState = new CurrentState<>(new Identifier<Jvm>(jpaJvm.getId()), jpaJvm.getState(),
                            new DateTime(jpaJvm.getLastUpdateDate()), StateType.JVM, jpaJvm.getErrorStatus());
                    LOGGER.debug("Sending JVM state from db: {}", currentState);
                    messagingService.send(currentState);
                } else {
                    LOGGER.debug("Sending in-memory JVM state {}", inMemoryState);
                    messagingService.send(inMemoryState);
                }
            }
        return jpaJvmList.size();
    }
}
