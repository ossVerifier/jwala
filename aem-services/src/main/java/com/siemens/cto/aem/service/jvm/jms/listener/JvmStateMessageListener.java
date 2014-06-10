package com.siemens.cto.aem.service.jvm.jms.listener;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.jms.listener.message.JvmStateMapMessageConverter;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.service.jvm.JvmStateService;

public class JvmStateMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    private final JvmStateService jvmStateService;
    private final JvmStateMapMessageConverter converter;

    public JvmStateMessageListener(final JvmStateService theService,
                                   final JvmStateMapMessageConverter theConverter) {
        jvmStateService = theService;
        converter = theConverter;
    }

    public void onMessage(final Message message) {
        try {
            LOGGER.info("Received message : {}", message.getJMSMessageID());
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
        LOGGER.info("Processing message: {}", message);
        jvmStateService.setCurrentJvmState(message.toCommand(),
                                           User.getSystemUser());
    }
}
