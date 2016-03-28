package com.siemens.cto.aem.service.jvm.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
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
        try {
            response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            LOGGER.debug(">>> Response = {} from JVM {}", response.getStatusCode(), jvm.getId().getId());
            if (response.getStatusCode() == HttpStatus.OK) {
                jvmStateService.updateNotInMemOrStartedButStaleState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
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

    private CurrentState<Jvm, JvmState> verifyStateRemotelyAndDoAnUpdate(final Jvm jvm, final JvmStateService jvmStateService, final String errMsg) {
        final RemoteCommandReturnInfo remoteCommandReturnInfo = jvmStateService.getServiceStatus(jvm);
        LOGGER.debug("RemoteCommandReturnInfo = {}", remoteCommandReturnInfo);
        if ((remoteCommandReturnInfo.retCode == 0 && remoteCommandReturnInfo.standardOuput.contains(STOPPED))) {
            jvmStateService.updateNotInMemOrStartedButStaleState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
            return new CurrentState<>(jvm.getId(), JvmState.JVM_STOPPED, DateTime.now(), StateType.JVM);
        }
        jvmStateService.updateNotInMemOrStartedButStaleState(jvm, JvmState.JVM_FAILED, errMsg);
        return new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED, DateTime.now(), StateType.JVM, errMsg);
    }

}
