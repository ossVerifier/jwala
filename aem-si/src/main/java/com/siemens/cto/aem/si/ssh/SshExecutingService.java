package com.siemens.cto.aem.si.ssh;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.util.Assert;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.RemoteExecCommand;
import com.siemens.cto.aem.domain.model.exec.RemoteSystemConnection;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.exception.CommandFailureException;

public class SshExecutingService {

    private final BeanFactory beanFactory;
    private final CommandExecutor commandExecutor;
    private final JschBuilder jschBuilder;
    private final SshConfiguration sshConfiguration;
    private final Expression commandExpression;
    private final Expression hostExpression;
    private final EvaluationContext evaluationContext;

    public SshExecutingService(final BeanFactory theBeanFactory,
                               final CommandExecutor theCommandExecutor,
                               final JschBuilder theJschBuilder,
                               final SshConfiguration theSshConfiguration,
                               final String theCommand,
                               final String theHost) {
        Assert.hasText(theCommand, "'command' must not be null");
        Assert.hasText(theHost, "'host' must not be null");
        beanFactory = theBeanFactory;
        commandExecutor = theCommandExecutor;
        jschBuilder = theJschBuilder;
        sshConfiguration = theSshConfiguration;
        commandExpression = new LiteralExpression(theCommand);
        hostExpression = new LiteralExpression(theHost);
        evaluationContext = ExpressionUtils.createStandardEvaluationContext(beanFactory);
    }

    public SshExecutingService(final BeanFactory theBeanFactory,
                               final CommandExecutor theCommandExecutor,
                               final JschBuilder theJschBuilder,
                               final SshConfiguration theSshConfiguration,
                               final Expression theCommandExpression,
                               final Expression theHostExpression) {
        Assert.notNull(theCommandExpression, "'commandExpression' must not be null");
        Assert.notNull(theHostExpression, "'hostExpression' must not be null");
        beanFactory = theBeanFactory;
        commandExecutor = theCommandExecutor;
        jschBuilder = theJschBuilder;
        sshConfiguration = theSshConfiguration;
        commandExpression = theCommandExpression;
        hostExpression = theHostExpression;
        evaluationContext = ExpressionUtils.createStandardEvaluationContext(beanFactory);
    }

    public ExecData handleSshMessage(final Message<?> aMessage) {
        try {
            final CommandProcessorBuilder commandProcessorBuilder = createCommandProcessorBuilder(aMessage);
            final ExecData execData = commandExecutor.execute(commandProcessorBuilder);
            return execData;
        } catch (final CommandFailureException | JSchException e) {
            throw new MessagingException(aMessage,
                                         e);
        }
    }

    private CommandProcessorBuilder createCommandProcessorBuilder(final Message<?> aMessage) throws JSchException {
        final JschCommandProcessorBuilder builder = new JschCommandProcessorBuilder();
        builder.setJsch(jschBuilder.build());
        builder.setRemoteCommand(createRemoteCommand(aMessage));
        return builder;
    }

    private RemoteExecCommand createRemoteCommand(final Message<?> aMessage) {
        final RemoteExecCommand command = new RemoteExecCommand(createRemoteSystemConnection(aMessage),
                                                                createExecCommand(aMessage));
        return command;
    }

    private RemoteSystemConnection createRemoteSystemConnection(final Message<?> aMessage) {
        final RemoteSystemConnection connection = new RemoteSystemConnection(sshConfiguration.getUserName(),
                                                                             evaluateHostExpression(aMessage),
                                                                             sshConfiguration.getPort());
        return connection;
    }

    private ExecCommand createExecCommand(final Message<?> aMessage) {
        final ExecCommand command = new ExecCommand(evaluateCommandExpression(aMessage));
        return command;
    }

    private String evaluateHostExpression(final Message<?> aMessage) {
        return hostExpression.getValue(evaluationContext,
                                       aMessage,
                                       String.class);
    }

    private String evaluateCommandExpression(final Message<?> aMessage) {
        return commandExpression.getValue(evaluationContext,
                                          aMessage,
                                          String.class);
    }
}
