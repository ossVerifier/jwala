package com.siemens.cto.aem.service.jvm.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.service.MapWrapper;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
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

    private final ClientFactoryHelper clientFactoryHelper;
    private final MapWrapper<Identifier<Jvm>, CurrentState<Jvm, JvmState>> stateMapWrapper;
    private final GroupStateNotificationService groupStateNotificationService;

    @Autowired
    public JvmStateResolverWorker(final ClientFactoryHelper clientFactoryHelper,
                                  final MapWrapper<Identifier<Jvm>, CurrentState<Jvm, JvmState>> stateMapWrapper,
                                  final GroupStateNotificationService groupStateNotificationService) {
        this.clientFactoryHelper = clientFactoryHelper;
        this.stateMapWrapper = stateMapWrapper;
        this.groupStateNotificationService = groupStateNotificationService;
    }

    @Async("jvmTaskExecutor")
    public Future pingJvm(final Jvm jvm, final JvmStateService jvmStateService) {
        LOGGER.debug("+++ pingJvm");
        final JvmState jvmState = jvm.getState();
        ClientHttpResponse response = null;
        try {
            response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            LOGGER.debug(">>> Response = {} from JVM {}", response.getStatusCode(), jvm.getId().getId());
            if (response.getStatusCode() == HttpStatus.OK) {
                jvmStateService.setState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
            } else {
                if (!jvmState.equals(JvmState.JVM_NEW)) {
                    jvmStateService.setState(jvm, JvmState.JVM_STOPPED, "Request for '" + jvm.getStatusUri() +
                            "' failed with a response code of '" + response.getStatusCode() + "'");
                } else {
                    LOGGER.debug("Not setting jvm state to JVM_STOPPED because still in JVM_NEW state for {}", jvm);
                }
            }
        } catch (final IOException ioe) {
            if (ioe instanceof ConnectTimeoutException) {
                LOGGER.debug("{} {}", jvm.getJvmName(), ioe.getMessage(), ioe);
            } else {
                LOGGER.info("{} {}", jvm.getJvmName(), ioe.getMessage(), ioe);
            }

            if (!jvmState.equals(JvmState.JVM_NEW)) {
                jvmStateService.setState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
            }
        } catch (final RuntimeException rte) {
            LOGGER.error(rte.getMessage(), rte);
        } finally {
            if (response != null) {
                response.close();
                LOGGER.debug("response closed");
            }
            LOGGER.debug("--- pingJvm");
        }
        return new AsyncResult(null);
    }


}
