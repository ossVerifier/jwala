package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.ConcurrentHashMap;

public class JvmStateMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    private final JvmStateMapMessageConverter converter;
    private final JvmService jvmService;
    private static final ConcurrentHashMap<Identifier<Jvm>, JvmState> jvmLastPersistedStateMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Identifier<Jvm>, String> jvmLastPersistedErrorStatusMap = new ConcurrentHashMap<>();
    private final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc;

    public JvmStateMessageListener(final JvmStateMapMessageConverter theConverter,
                                   final JvmService jvmService,
                                   final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc) {
        converter = theConverter;
        this.jvmService = jvmService;
        this.grpStateComputationAndNotificationSvc = grpStateComputationAndNotificationSvc;
    }

    @Override
    public void onMessage(final Message message) {
        try {
            LOGGER.debug("Received message : {}", message.getJMSMessageID());
            handleMessage(message);
        } catch (final JMSException | RuntimeException e) {
            LOGGER.warn("Failure while handling a message; ignoring the message", e);
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

        if (!jvmLastPersistedStateMap.containsKey(newState.getId()) ||
            !jvmLastPersistedStateMap.get(newState.getId()).equals(newState.getState())) {
                jvmLastPersistedStateMap.put(newState.getId(), newState.getState());
                stateAndOrMsgChanged = true;
        }

        final String msg = newState.getMessage();
        if (StringUtils.isNotEmpty(msg) && (!jvmLastPersistedErrorStatusMap.containsKey(newState.getId()) ||
            !jvmLastPersistedErrorStatusMap.get(newState.getId()).equals(msg))) {
                jvmLastPersistedErrorStatusMap.put(newState.getId(), msg);
                stateAndOrMsgChanged = true;
        }

        if (stateAndOrMsgChanged) {
            jvmService.updateState(newState.getId(), newState.getState(), msg);
            grpStateComputationAndNotificationSvc.computeAndNotify(newState.getId(), newState.getState());
        }
    }

}
