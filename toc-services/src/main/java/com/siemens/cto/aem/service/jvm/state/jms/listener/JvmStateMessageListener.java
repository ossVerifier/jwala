package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
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
    private final JvmPersistenceService jvmPersistenceService;
    private static final ConcurrentHashMap<Identifier<Jvm>, JvmState> jvmLastPersistedStateMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Identifier<Jvm>, String> jvmLastPersistedErrorStatusMap = new ConcurrentHashMap<>();

    public JvmStateMessageListener(final JvmStateMapMessageConverter theConverter,
                                   final JvmPersistenceService jvmPersistenceService) {
        converter = theConverter;
        this.jvmPersistenceService = jvmPersistenceService;
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

        // This is for the state
        if (jvmLastPersistedStateMap.containsKey(newState.getId())) {
            if (!jvmLastPersistedStateMap.get(newState.getId()).equals(newState.getState())) {
                jvmLastPersistedStateMap.put(newState.getId(), newState.getState());
                jvmPersistenceService.updateState(newState.getId(), newState.getState().toStateLabel());
            }
        } else {
            jvmLastPersistedStateMap.put(newState.getId(), newState.getState());
            jvmPersistenceService.updateState(newState.getId(), newState.getState().toStateLabel());
        }

        // This is for the error status
        if (StringUtils.isNotEmpty(newState.getMessage())) {
            if (jvmLastPersistedErrorStatusMap.containsKey(newState.getId())) {
                if (!jvmLastPersistedErrorStatusMap.get(newState.getId()).equals(newState.getMessage())) {
                    jvmLastPersistedErrorStatusMap.put(newState.getId(), newState.getMessage());
                    jvmPersistenceService.updateErrorStatus(newState.getId(), newState.getMessage());
                }
            } else {
                jvmLastPersistedErrorStatusMap.put(newState.getId(), newState.getMessage());
                jvmPersistenceService.updateErrorStatus(newState.getId(), newState.getMessage());
            }
        }

    }
}
