package com.siemens.cto.aem.service.dispatch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.GroupDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.SplitterTransformer;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;

public class CommandDecomposerBeanImpl implements SplitterTransformer {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CommandDecomposerBeanImpl.class);

    public Collection<Message<? extends DispatchCommand>> split(Message<? extends DispatchCommand> command) {
        List<Message<? extends DispatchCommand>> newMessages = new ArrayList<Message<? extends DispatchCommand>>();

        UUID correlationId = command.getHeaders().getId();
        LOGGER.info("Decomposition correlation id: {}", correlationId.toString());

        Object payload = command.getPayload();
        if (GroupDispatchCommand.class.isAssignableFrom(payload.getClass())) {

            GroupDispatchCommand groupDispatchCommand = (GroupDispatchCommand) payload;
            Long controlIdentity = groupDispatchCommand.getIdentity();

            List<DispatchCommand> results = splitGroup(groupDispatchCommand);
            int numMessages = results.size();
            int msgIndex = 0;

            for (DispatchCommand msg : results) {
                Message<? extends DispatchCommand> newMessage = MessageBuilder.withPayload(msg)
                        .setHeader("TocDispatchControlId", controlIdentity).copyHeaders(command.getHeaders())
                        .pushSequenceDetails(correlationId, msgIndex++, numMessages).build();
                LOGGER.info("Decomposed into {}", newMessage);
                newMessages.add(newMessage);
            }

        } else {
            LOGGER.info("Message is not splittable {}", command);
            newMessages.add(command);
        }

        return newMessages;
    }

    // later this will get more complex and splitting groups will depend on the
    // command and should be done elsewhere.
    protected List<DispatchCommand> splitGroup(GroupDispatchCommand groupDispatchCommand) {
        List<DispatchCommand> jvmCommands = new ArrayList<DispatchCommand>();

        Group group = groupDispatchCommand.getGroup();

        for (Jvm jvm : group.getJvms()) {
            ControlJvmCommand controlCommand = new ControlJvmCommand(jvm.getId(), JvmControlOperation.START);
            jvmCommands.add(new JvmDispatchCommand(jvm, controlCommand, groupDispatchCommand.getUser()));
        }

        return jvmCommands;
    }
}
