package com.siemens.cto.aem.service.jvm.jms.sender;

import javax.jms.Destination;

import org.springframework.jms.core.JmsTemplate;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.jms.sender.message.JvmStateUpdatedMessageCreator;

public class JmsJvmStateNotificationServiceImpl implements JvmStateNotificationService {

    private final JmsTemplate template;
    private final Destination destination;

    public JmsJvmStateNotificationServiceImpl(final JmsTemplate theTemplate,
                                              final Destination theDestination) {
        template = theTemplate;
        destination = theDestination;
    }

    @Override
    public void notifyJvmStateUpdated(final Identifier<Jvm> aJvmId) {
        template.send(destination,
                      new JvmStateUpdatedMessageCreator(aJvmId));
    }
}
