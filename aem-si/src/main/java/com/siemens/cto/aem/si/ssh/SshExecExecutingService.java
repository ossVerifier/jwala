package com.siemens.cto.aem.si.ssh;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.Expression;
import org.springframework.integration.Message;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;

public class SshExecExecutingService extends SshExecutingService {

    public SshExecExecutingService(final BeanFactory theBeanFactory,
                                   final CommandExecutor theCommandExecutor,
                                   final JschBuilder theJschBuilder,
                                   final SshConfiguration theSshConfiguration,
                                   final Expression theCommandExpression,
                                   final Expression theHostExpression) {
        super(theBeanFactory,
              theCommandExecutor,
              theJschBuilder,
              theSshConfiguration,
              theCommandExpression,
              theHostExpression);
    }

    @Override
    protected ExecCommand createExecCommand(final Message<?> aMessage) {
        final ExecCommand command = commandExpression.getValue(evaluationContext,
                                                               aMessage,
                                                               ExecCommand.class);
        return command;
    }
}
