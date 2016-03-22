package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JvmStateMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    @Value("${spring.messaging.topic.serverStates:/topic/server-states}")
    protected String topicServerStates;

    private final JvmStateMapMessageConverter converter;
    private final JvmService jvmService;
    private static final Map<Identifier<Jvm>, JvmState> JVM_LAST_PERSISTED_STATE_MAP = new ConcurrentHashMap<>();
    private static final Map<Identifier<Jvm>, String> JVM_LAST_PERSISTED_ERROR_STATUS_MAP = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupStateNotificationService groupStateNotificationService;

    public JvmStateMessageListener(final JvmStateMapMessageConverter theConverter,
                                   final JvmService jvmService,
                                   final SimpMessagingTemplate messagingTemplate,
                                   final GroupStateNotificationService groupStateNotificationService) {
        converter = theConverter;
        this.jvmService = jvmService;
        this.messagingTemplate = messagingTemplate;
        this.groupStateNotificationService = groupStateNotificationService;
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

    @SuppressWarnings("unchecked")
    protected void processMessage(final MapMessage aMapMessage) throws JMSException {
        final JvmStateMessage message = converter.convert(aMapMessage);
        LOGGER.debug("Processing message: {}", message);

        final SetStateRequest<Jvm, JvmState> setStateCommand = message.toCommand();
        final CurrentState<Jvm, JvmState> newState = setStateCommand.getNewState();

        if (isStateChangedAndOrMsgNotEmpty(newState)) {
            jvmService.updateState(newState.getId(), newState.getState(), newState.getMessage());
            messagingTemplate.convertAndSend(topicServerStates, new CurrentState(newState.getId(), newState.getState(),
                    DateTime.now(), StateType.JVM, newState.getMessage()));
            groupStateNotificationService.retrieveStateAndSendToATopic(newState.getId(), Jvm.class, topicServerStates);
        }
    }

    /**
     * Check if the state has changed and-or message is not empty.
     *
     * @param newState the latest state
     * @return returns true if the state is not the same compared to the previous state or if there's a message (error message)
     */
    private boolean isStateChangedAndOrMsgNotEmpty(CurrentState<Jvm, JvmState> newState) {
        boolean stateAndOrMsgChanged = false;

        if (!JVM_LAST_PERSISTED_STATE_MAP.containsKey(newState.getId()) ||
            !JVM_LAST_PERSISTED_STATE_MAP.get(newState.getId()).equals(newState.getState())) {
                JVM_LAST_PERSISTED_STATE_MAP.put(newState.getId(), newState.getState());
                stateAndOrMsgChanged = true;
        }

        if (StringUtils.isNotEmpty(newState.getMessage()) && (!JVM_LAST_PERSISTED_ERROR_STATUS_MAP.containsKey(newState.getId()) ||
            !JVM_LAST_PERSISTED_ERROR_STATUS_MAP.get(newState.getId()).equals(newState.getMessage()))) {
                JVM_LAST_PERSISTED_ERROR_STATUS_MAP.put(newState.getId(), newState.getMessage());
                stateAndOrMsgChanged = true;
        }
        return stateAndOrMsgChanged;
    }

}
