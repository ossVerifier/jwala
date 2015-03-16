package com.siemens.cto.aem.service.jvm.state.jms.listener;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.service.state.StateService;

public class JvmStateMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    private final StateService<Jvm, JvmState> jvmStateService;
    private final JvmStateMapMessageConverter converter;

    public JvmStateMessageListener(final StateService<Jvm, JvmState> theService,
                                   final JvmStateMapMessageConverter theConverter) {
        jvmStateService = theService;
        converter = theConverter;
    }

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
        SetStateCommand<Jvm, JvmState> setStateCommand = message.toCommand();
        CurrentState<Jvm, JvmState> newState  = setStateCommand.getNewState();
        CurrentState<Jvm, JvmState> currentState = jvmStateService.getCurrentState(newState.getId());

        boolean discard = false;

        // if starting, ignore stopped. If stopping, ignore started.
        // TODO: we rely on started to exit starting. However, we have no way to leave STARTING to STOPPED in case of failure.
        if(currentState != null) {
            switch(currentState.getState()) {
                case START_REQUESTED:
                    switch(newState.getState()) {
                        case STOPPED: 
                            discard = true;
                            break;
                        default: break;
                    } break;
                case STOP_REQUESTED:
                    switch(newState.getState()) {
                        case STARTED: 
                            discard = true;
                            break;
                        default: break;
                    } break;
                default:
                    discard = false;   
                    break;
            }
        }
        
        if(discard) { 
            LOGGER.debug("Discarding message; Jvm starting: {}", message);                
        } else { 
            jvmStateService.setCurrentState(message.toCommand(),
                                            User.getSystemUser());
        }
    }
}
