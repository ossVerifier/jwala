package com.siemens.cto.aem.service.dispatch.impl;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;

public class JvmCommandExecutorBeanImpl {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JvmCommandExecutorBeanImpl.class);

    public Message<CompleteControlJvmCommand> deploy(Message<? extends DispatchCommand> msg) {

        LOGGER.info("Would execute: " + msg.getPayload().toString() + " with correlationId = " + msg.getHeaders().getCorrelationId());

        ExecData execData = new ExecData(new ExecReturnCode(0), "Job Complete", "");
        Long correlationId = (Long) msg.getHeaders().get("TocDispatchControlId");
        Identifier<JvmControlHistory> identifier = new Identifier<JvmControlHistory>(correlationId);
        CompleteControlJvmCommand result = new CompleteControlJvmCommand(identifier, execData);

        Message<CompleteControlJvmCommand> resultMessage = MessageBuilder
                .withPayload((CompleteControlJvmCommand) result).copyHeaders(msg.getHeaders()).build();

        LOGGER.info("Fake Return value : " + execData);
        return resultMessage;
    }
}
