package com.siemens.cto.aem.service.jvm.state.jms.listener;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

public class JvmStateMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    private final JvmStateMapMessageConverter converter;
    private final JvmService jvmService;
    private final MessagingService messagingService;
    private final GroupStateNotificationService groupStateNotificationService;
    private final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService;

    public JvmStateMessageListener(final JvmStateMapMessageConverter converter,
                                   final JvmService jvmService,
                                   final MessagingService messagingTemplate,
                                   final GroupStateNotificationService groupStateNotificationService,
                                   final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService) {
        this.converter = converter;
        this.jvmService = jvmService;
        this.messagingService = messagingTemplate;
        this.groupStateNotificationService = groupStateNotificationService;
        this.inMemoryStateManagerService = inMemoryStateManagerService;
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
            LOGGER.debug("The state has changed from {} to {}", inMemoryStateManagerService.get(newState.getId()) != null ?
                    inMemoryStateManagerService.get(newState.getId()).getState() : "NO STATE", newState.getState());
            jvmService.updateState(newState.getId(), newState.getState(), newState.getMessage());
            messagingService.send(new CurrentState(newState.getId(), newState.getState(), DateTime.now(), StateType.JVM,
                    newState.getMessage()));
            groupStateNotificationService.retrieveStateAndSendToATopic(newState.getId(), Jvm.class);
        }
        // Always update the JVM state map even if the state did not change since there's another thread that checks if the state is stale of not!
        inMemoryStateManagerService.put(newState.getId(), newState);
    }

    /**
     * Check if the state has changed and-or message is not empty.
     *
     * @param newState the latest state
     * @return returns true if the state is not the same compared to the previous state or if there's a message (error message)
     */
    private boolean isStateChangedAndOrMsgNotEmpty(CurrentState<Jvm, JvmState> newState) {
        boolean stateAndOrMsgChanged = false;

        if (!inMemoryStateManagerService.containsKey(newState.getId()) ||
            !inMemoryStateManagerService.get(newState.getId()).getState().equals(newState.getState())) {
                stateAndOrMsgChanged = true;
        }

        if (StringUtils.isNotEmpty(newState.getMessage()) && (!inMemoryStateManagerService.containsKey(newState.getId()) ||
            !inMemoryStateManagerService.get(newState.getId()).getMessage().equals(newState.getMessage()))) {
                stateAndOrMsgChanged = true;
        }
        return stateAndOrMsgChanged;
    }

}
