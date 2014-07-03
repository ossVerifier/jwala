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

            List<DispatchCommand> results = splitGroupToJvmCommands(groupDispatchCommand);
            int numMessages = results.size();
            int msgIndex = 0;

            for (DispatchCommand msg : results) {
                Message<? extends DispatchCommand> newMessage = MessageBuilder.withPayload(msg)
                        .setHeader("GroupDispatchCommand", groupDispatchCommand).copyHeaders(command.getHeaders())
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

    public List<DispatchCommand> splitGroupToJvmCommands(GroupDispatchCommand groupDispatchCommand) {
        List<DispatchCommand> jvmCommands = new ArrayList<DispatchCommand>();
        JvmControlOperation jvmControlOperation = JvmControlOperation.convertFrom(groupDispatchCommand.getCommand().getControlOperation().getExternalValue());

        Group group = groupDispatchCommand.getGroup();

        for (Jvm jvm : group.getJvms()) {
            ControlJvmCommand controlCommand = new ControlJvmCommand(jvm.getId(), jvmControlOperation);
            jvmCommands.add(new JvmDispatchCommand(jvm, controlCommand, groupDispatchCommand.getUser()));
        }

        return jvmCommands;
    }

    @Override
    public List<DispatchCommand> splitGroupToDeployCommands(GroupDispatchCommand groupDispatchCommand) {
        return null;
    }
}
