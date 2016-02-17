package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.StateNotificationService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JvmStateMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    private final JvmStateMapMessageConverter converter;
    private final JvmService jvmService;
    private static final Map<Identifier<Jvm>, JvmState> JVM_LAST_PERSISTED_STATE_MAP = new ConcurrentHashMap<>();
    private static final Map<Identifier<Jvm>, String> JVM_LAST_PERSISTED_ERROR_STATUS_MAP = new ConcurrentHashMap<>();
    private final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc;
    private final StateNotificationService stateNotificationService;

    public JvmStateMessageListener(final JvmStateMapMessageConverter theConverter,
                                   final JvmService jvmService,
                                   final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc,
                                   final StateNotificationService stateNotificationService) {
        converter = theConverter;
        this.jvmService = jvmService;
        this.grpStateComputationAndNotificationSvc = grpStateComputationAndNotificationSvc;
        this.stateNotificationService = stateNotificationService;
    }

    @Override
    public void onMessage(final Message message) {
        try {
            LOGGER.debug("Received message : {}", message.getJMSMessageID());
            handleMessage(message);
        } catch (final JMSException | RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void handleMessage(final Message aMessage) throws JMSException {
        if (aMessage instanceof MapMessage) {
            processMessage((MapMessage) aMessage);
        } else {
            LOGGER.warn("Unable to process message {} of type {} ", aMessage.getJMSMessageID(), aMessage.getClass().getName());
        }
    }

    protected void processMessage(final MapMessage aMapMessage) throws JMSException {
        final JvmStateMessage message = converter.convert(aMapMessage);
        LOGGER.debug("Processing message: {}", message);

        final SetStateRequest<Jvm, JvmState> setStateCommand = message.toCommand();
        final CurrentState<Jvm, JvmState> newState = setStateCommand.getNewState();
        boolean stateAndOrMsgChanged = false;

        if (!JVM_LAST_PERSISTED_STATE_MAP.containsKey(newState.getId()) ||
            !JVM_LAST_PERSISTED_STATE_MAP.get(newState.getId()).equals(newState.getState())) {
                JVM_LAST_PERSISTED_STATE_MAP.put(newState.getId(), newState.getState());
                stateAndOrMsgChanged = true;
        }

        final String msg = newState.getMessage();
        if (StringUtils.isNotEmpty(msg) && (!JVM_LAST_PERSISTED_ERROR_STATUS_MAP.containsKey(newState.getId()) ||
            !JVM_LAST_PERSISTED_ERROR_STATUS_MAP.get(newState.getId()).equals(msg))) {
                JVM_LAST_PERSISTED_ERROR_STATUS_MAP.put(newState.getId(), msg);
                stateAndOrMsgChanged = true;
        }

        if (stateAndOrMsgChanged) {
            stateNotificationService.notifyStateUpdated(new CurrentState(newState.getId(), newState.getState(),
                    DateTime.now(), StateType.JVM, msg));
            jvmService.updateState(newState.getId(), newState.getState(), msg);
            grpStateComputationAndNotificationSvc.computeAndNotify(newState.getId(), newState.getState());
        }
    }

}
