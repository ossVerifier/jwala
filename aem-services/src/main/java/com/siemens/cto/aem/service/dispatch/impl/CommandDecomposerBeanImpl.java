package com.siemens.cto.aem.service.dispatch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.SplittableDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.SplitterTransformer;

public class CommandDecomposerBeanImpl implements SplitterTransformer {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CommandDecomposerBeanImpl.class);

    public Collection<Message<? extends DispatchCommand>> split(Message<SplittableDispatchCommand> command) {
        List<Message<? extends DispatchCommand>> newMessages = new ArrayList<Message<? extends DispatchCommand>>();

        SplittableDispatchCommand splittableDispatchCommand = command.getPayload();

        Long controlIdentity = splittableDispatchCommand.getIdentity();

        UUID correlationId = command.getHeaders().getId();
        LOGGER.info("Decomposition correlation id: {}", correlationId.toString());

        List<DispatchCommand> results = splittableDispatchCommand.getSubCommands(this);
        int numMessages = results.size();
        int msgIndex = 0;

        for (DispatchCommand msg : results) {
            Message<? extends DispatchCommand> newMessage = MessageBuilder.withPayload(msg)
                    .setHeader("TocDispatchControlId", controlIdentity).copyHeaders(command.getHeaders())
                    .pushSequenceDetails(correlationId, msgIndex++, numMessages).build();
            LOGGER.info("Decomposed into {}", newMessage);
            newMessages.add(newMessage);
        }

        return newMessages;
    }
}
