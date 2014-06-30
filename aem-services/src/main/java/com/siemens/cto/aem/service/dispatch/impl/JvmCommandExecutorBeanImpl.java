package com.siemens.cto.aem.service.dispatch.impl;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.siemens.cto.aem.domain.model.dispatch.GroupDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.service.jvm.JvmControlService;

public class JvmCommandExecutorBeanImpl {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JvmCommandExecutorBeanImpl.class);

    private JvmControlService jvmControlService;

    public JvmCommandExecutorBeanImpl(JvmControlService theJvmControlService) {
        jvmControlService = theJvmControlService;
    }

    public Message<JvmDispatchCommandResult> deploy(Message<JvmDispatchCommand> msg) {

        JvmDispatchCommand jvmDispatchCommand = msg.getPayload();
        GroupDispatchCommand groupDispatchCommand = (GroupDispatchCommand) msg.getHeaders().get("GroupDispatchCommand");

        LOGGER.info("Execute command : " + jvmDispatchCommand.toString() + " with commandDispatchId = " + groupDispatchCommand.getIdentity());

        JvmControlHistory jvmControlHistory = jvmControlService.controlJvm(jvmDispatchCommand.getCommand(),
                jvmDispatchCommand.getUser());
        // deal with Runtime exception thrown here...

        // NPE??
        Boolean wasSuccessful = jvmControlHistory.getExecData().getReturnCode().getWasSuccessful();
        JvmDispatchCommandResult result = new JvmDispatchCommandResult(wasSuccessful, jvmControlHistory.getId());

        return MessageBuilder.withPayload((JvmDispatchCommandResult) result).copyHeaders(msg.getHeaders()).build();
    }
}
