package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sends notification to the group state machine that a web server/jvm state has changed state.
 * The group state machine then computes the state of affected group(s).
 * The aim is to replace state notification implemented with spring integration.
 *
 * Created by JC043760 on 11/4/2015.
 */
@Service("stateNotificationWorker")
public class StateNotificationWorkerImpl implements StateNotificationWorker {
    private static final Logger logger = LoggerFactory.getLogger(StateNotificationWorkerImpl.class);

    @Override
    @Async("stateNotificationWorkerTaskExecutor")
    public void sendStateChangeNotification(final GroupStateService.API groupStateService,
        final CurrentState currentState) {
        if (currentState.getState() instanceof JvmState) {
            jvmStateChanged(groupStateService, currentState);
        } else if (currentState.getState() instanceof WebServerReachableState) {
            webServerStateChanged(groupStateService, currentState);
        }
    }

    @Override
    @Async("stateNotificationWorkerTaskExecutor")
    // Note: This causes the group unknown problem (New group status does not change to the proper state).
    // TODO: Find out if we really need this, if not remove it.
    public void refreshState(final GroupStateService.API groupStateService, final Group group) {
        try {
            groupStateService.stateUpdateRequest(group);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void jvmStateChanged(final GroupStateService.API groupStateService,
                                 final CurrentState<Jvm, JvmState> jvmState) {

        try {
            final List<SetGroupStateCommand> setGroupStateCommandList = groupStateService.stateUpdateJvm(jvmState);
            updateGroupState(groupStateService, setGroupStateCommandList);
        } catch (InterruptedException e) {
            // Log it for now!
            // TODO: Find out if we need to rethrow.
            logger.error(e.getMessage(), e);
        }
    }

    private void webServerStateChanged(final GroupStateService.API groupStateService,
                                       final CurrentState<WebServer, WebServerReachableState> webServerState) {
        try {
            final List<SetGroupStateCommand> setGroupStateCommandList =  groupStateService.stateUpdateWebServer(webServerState);
            updateGroupState(groupStateService, setGroupStateCommandList);
        } catch (InterruptedException e) {
            // Log it for now!
            // TODO: Find out if we need to rethrow.
            logger.error(e.getMessage(), e);
        }
    }

    private void updateGroupState(final GroupStateService.API groupStateService,
                                  final List<SetGroupStateCommand> setGroupStateCommandList) {
        for (SetGroupStateCommand setGroupStateCommand: setGroupStateCommandList) {
            try {
                groupStateService.groupStatePersist(setGroupStateCommand);
            } finally {
                groupStateService.groupStateUnlock(setGroupStateCommand);
            }
            groupStateService.groupStateNotify(setGroupStateCommand);
        }
    }

}
