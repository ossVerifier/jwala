package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.common.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.common.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.common.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.service.jvm.JvmControlService;

/**
 * Executes a JVM related command.
 * Note: This bean is used in conjunction with Spring integration which is responsible for splitting multiple commands
 *       of different JVMs.
 */
public class JvmCommandExecutorBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JvmCommandExecutorBean.class);

    private JvmControlService jvmControlService;

    public JvmCommandExecutorBean(JvmControlService theJvmControlService) {
        jvmControlService = theJvmControlService;
    }

    public JvmDispatchCommandResult startStop(JvmDispatchCommand jvmDispatchCommand) {
        GroupJvmDispatchCommand groupDispatchCommand = jvmDispatchCommand.getGroupJvmDispatchCommand();
        LOGGER.debug("Execute command : {}", jvmDispatchCommand.toString());
        Boolean wasSuccessful;
        try {
            ControlJvmRequest controlJvmCommand = new ControlJvmRequest(jvmDispatchCommand.getJvm().getId(),
                    groupDispatchCommand.getRequest().getControlOperation());

            CommandOutput commandOutput = jvmControlService.controlJvm(controlJvmCommand, groupDispatchCommand.getUser());
            wasSuccessful = commandOutput.getReturnCode().getWasSuccessful();
            LOGGER.debug("ControlJvmRequest complete.  Success = {}", wasSuccessful);
            
        } catch (final RuntimeException e) {
            wasSuccessful = false;
            LOGGER.error("Group dispatch {}; ControlJvmRequest {}", groupDispatchCommand.toString(), jvmDispatchCommand.toString(), e);
        }
        return new JvmDispatchCommandResult(wasSuccessful, groupDispatchCommand);
    }

}
