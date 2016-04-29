package com.siemens.cto.aem.service.jvm.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
import com.siemens.cto.aem.service.exception.RemoteCommandExecutorServiceException;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Resolves a JVM's state.
 * NOTE: For @Async to work, the worker must be in its own class separate from the caller the reason for this
 *       class' existence.
 *
 * Created by JC043760 on 3/24/2016.
 */
@Service
public class JvmStateResolverWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateResolverWorker.class);
    private static final String STOPPED = "STOPPED";

    private final ClientFactoryHelper clientFactoryHelper;

    @Autowired
    public JvmStateResolverWorker(final ClientFactoryHelper clientFactoryHelper) {
        this.clientFactoryHelper = clientFactoryHelper;
    }

    @Async("jvmTaskExecutor")
    public Future<CurrentState<Jvm, JvmState>> pingAndUpdateJvmState(final Jvm jvm, final JvmStateService jvmStateService) {
        LOGGER.debug("+++ pingAndUpdateJvmState");
        ClientHttpResponse response = null;
        CurrentState<Jvm, JvmState> currentState = null;

        // if the jvm was just created do not check its state
        if (jvm.getState().equals(JvmState.JVM_NEW)){
            return new AsyncResult<>(new CurrentState<>(jvm.getId(), jvm.getState(), DateTime.now(), StateType.JVM));
        }

        try {
            response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            LOGGER.debug(">>> Response = {} from JVM {}", response.getStatusCode(), jvm.getId().getId());
            if (response.getStatusCode() == HttpStatus.OK) {
                jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
                currentState = new CurrentState<>(jvm.getId(), JvmState.JVM_STARTED, DateTime.now(), StateType.JVM);
            } else {
                currentState = verifyStateRemotelyAndDoAnUpdate(jvm, jvmStateService, "Request for '" + jvm.getStatusUri() +
                        "' failed with a response code of '" + response.getStatusCode() + "'");
            }
        } catch (final IOException ioe) {
            LOGGER.debug("{} {}", jvm.getJvmName(), ioe.getMessage(), ioe);
            currentState = verifyStateRemotelyAndDoAnUpdate(jvm, jvmStateService, ioe.getMessage());
        } catch (final RuntimeException rte) {
            // This method is executed asynchronously and we do not want to interrupt the thread's lifecycle.
            LOGGER.error(rte.getMessage(), rte);
        } finally {
            if (response != null) {
                response.close();
                LOGGER.debug("response closed");
            }
            LOGGER.debug("--- pingAndUpdateJvmState");
        }
        return new AsyncResult<>(currentState);
    }

    /**
     * Verify the state in the remote server then do a state update.
     * @param jvm the JVM
     * @param jvmStateService {@link JvmStateService}
     * @param errMsg an error message form the caller which will be included in the state update if it gets verified
     * @return {@link CurrentState}
     */
    protected CurrentState<Jvm, JvmState> verifyStateRemotelyAndDoAnUpdate(final Jvm jvm, final JvmStateService jvmStateService, String errMsg) {
        try {
            final RemoteCommandReturnInfo remoteCommandReturnInfo = jvmStateService.getServiceStatus(jvm);
            LOGGER.debug("RemoteCommandReturnInfo = {}", remoteCommandReturnInfo);
            if ((remoteCommandReturnInfo.retCode == 0 && remoteCommandReturnInfo.standardOuput.contains(STOPPED))) {
                jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
                return new CurrentState<>(jvm.getId(), JvmState.JVM_STOPPED, DateTime.now(), StateType.JVM);
            }
        } catch (final RemoteCommandExecutorServiceException rcese) {
            LOGGER.error("Verify state in remote server failed!", rcese);
            errMsg = errMsg + ";" + rcese.getMessage();
        }

        // The state should be unknown if we can't verify the JVM's state.
        // In addition, if we just leave the state as is and just report an error, if that state is in started,
        // the state resolver (reverse heartbeat) will always end up trying to ping it which will eat CPU resources.
        final JvmState state = JvmState.JVM_UNKNOWN;
        jvmStateService.updateNotInMemOrStaleState(jvm, state, errMsg);
        return new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM, errMsg);
    }

}
