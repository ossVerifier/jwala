package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
    private static final String NOT_RECEIVING_JVM_STATE_ERR_MSG = "The JVM state listener is not receiving any state. " +
            "Possible causes are messaging settings are wrong, messaging server is down, JVM is not functioning correctly " +
            "(configuration error(s) etc) even though the service is running. Please see logs for details.";
    private static final String VERIFY_JVM_SERVICE_STOPPED_RETRIES = "verify.jvm.service.stopped.retries";
    private static final String VERIFY_JVM_SERVICE_STOPPED_RETRY_INTERVAL = "verify.jvm.service.stopped.retry.interval";
    private static final String VERIFY_JVM_SERVICE_STOPPED_RETRIES_DEFAULT_VALUE = "3";
    private static final String VERIFY_JVM_SERVICE_STOPPED_RETRY_INTERVAL_DEFAULT_VALUE = "2000";
    private static final String RUNNING = "RUNNING";

    private final ClientFactoryHelper clientFactoryHelper;

    @Autowired
    public JvmStateResolverWorker(final ClientFactoryHelper clientFactoryHelper) {
        this.clientFactoryHelper = clientFactoryHelper;
    }

    @Async("jvmTaskExecutor")
    public Future<CurrentState<Jvm, JvmState>> pingAndUpdateJvmState(final Jvm jvm, final JvmStateService jvmStateService) {
        LOGGER.debug("The reverse heartbeat has kicked in! This means that we're not receiving any states from Jvm {}@{}.",
                jvm.getJvmName(), jvm.getHostName());
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
                currentState = verifyIfJvmWinServiceIsStoppedAndDoAnUpdate(jvm, jvmStateService);
            }
        } catch (final IOException ioe) {
            LOGGER.error("{} {}", jvm.getJvmName(), ioe.getMessage(), ioe);
            currentState = verifyIfJvmWinServiceIsStoppedAndDoAnUpdate(jvm, jvmStateService);
        } catch (final RuntimeException rte) {
            // This method is executed asynchronously and we do not want to interrupt the thread's lifecycle so we
            // just catch and log runtime exceptions instead of rethrowing it
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
     * Verify if the JVM Window's service is stopped and update the state.
     * This method was intended to be called after an unsuccessful ping.
     * It verifies if the Windows service is stopped and if it is, then we can say that the JVM is stopped.
     * If the Window's service is not stopped and then we set the JVM state to UNKNOWN.
     * The reason for setting the JVM state to UNKNOWN even if the Window's service is running is because
     * the Window's service state is NOT THE SAME as the JVM state. There can be a case where the Window's service is
     * running but the JVM is not running as it should, meaning it's not serving the web applications and it's not
     * sending state messages.
     *
     * @param jvm the JVM
     * @param jvmStateService {@link JvmStateService}
     * @return {@link CurrentState}
     */
    private CurrentState<Jvm, JvmState> verifyIfJvmWinServiceIsStoppedAndDoAnUpdate(final Jvm jvm, final JvmStateService jvmStateService) {
        String errMsg;
        RemoteCommandReturnInfo remoteCommandReturnInfo;
        final int retries = Integer.parseInt(ApplicationProperties.get(VERIFY_JVM_SERVICE_STOPPED_RETRIES,
                                                                       VERIFY_JVM_SERVICE_STOPPED_RETRIES_DEFAULT_VALUE));
        final long interval = Long.parseLong(ApplicationProperties.get(VERIFY_JVM_SERVICE_STOPPED_RETRY_INTERVAL,
                                                                       VERIFY_JVM_SERVICE_STOPPED_RETRY_INTERVAL_DEFAULT_VALUE));
        try {
            final List<RemoteCommandReturnInfo> returnInfoList = new ArrayList<>();
            for (int i = 0; i < retries; i++) {
                remoteCommandReturnInfo = jvmStateService.getServiceStatus(jvm);
                LOGGER.debug("RemoteCommandReturnInfo = {}", remoteCommandReturnInfo);
                if (remoteCommandReturnInfo.retCode == 0 && remoteCommandReturnInfo.standardOuput.contains(STOPPED)) {
                    jvmStateService.updateNotInMemOrStaleState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
                    return new CurrentState<>(jvm.getId(), JvmState.JVM_STOPPED, DateTime.now(), StateType.JVM);
                }

                if (remoteCommandReturnInfo.standardOuput.contains(RUNNING)) {
                    LOGGER.error("Can't ping JVM {} and yet its service is running. We can't be certain what state it's in hence the state is UNKNOWN! Service query result = {}", jvm.getJvmName(),
                            remoteCommandReturnInfo);
                } else {
                    LOGGER.error("Failed to verify the state of JVM {}! Service query result = {}", jvm.getJvmName(),
                            remoteCommandReturnInfo);
                }

                returnInfoList.add(remoteCommandReturnInfo);

                try {
                    Thread.sleep(interval);
                } catch (final InterruptedException e) {
                    LOGGER.error("Sleep interrupted!", e);
                }
            }

            errMsg = MessageFormat.format("{0} Service status inquiry for JVM {1} = {2}", NOT_RECEIVING_JVM_STATE_ERR_MSG,
                    jvm.getJvmName(), returnInfoList.get(returnInfoList.size() - 1)); // just give the client the last retry result since all the results are in the logs anyways

        } catch (final RemoteCommandExecutorServiceException rcese) {
            errMsg = MessageFormat.format("{0} RemoteCommandExecutorServiceException thrown = {1}", NOT_RECEIVING_JVM_STATE_ERR_MSG,
                                           ExceptionUtils.getStackTrace(rcese));
            LOGGER.error(errMsg, rcese);
        }

        // The state should be unknown if we can't really verify the JVM's state.
        final JvmState state = JvmState.JVM_UNKNOWN;
        jvmStateService.updateNotInMemOrStaleState(jvm, state, errMsg);
        return new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM);
    }

}
