package com.siemens.cto.aem.commandprocessor.impl.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.exception.RemoteNotYetReturnedException;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.exec.RemoteExecCommand;
import com.siemens.cto.aem.domain.model.exec.RemoteSystemConnection;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;

public class JschCommandProcessorImpl implements CommandProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JschCommandProcessorImpl.class);
    
    private final Session session;
    private final Channel channel;
    private final InputStream remoteOutput;
    private final InputStream remoteError;
    private final OutputStream localInput;
    private final RemoteExecCommand remoteCommand;

    public JschCommandProcessorImpl(final JSch theJsch,
                                    final RemoteExecCommand theCommand) throws RemoteCommandFailureException {

        try {
            logger.debug("before executing command {}", theCommand);

            String commandString = theCommand.getCommand().toCommandString();
            logger.debug("remote Jsch command string is {}", commandString );
            
            remoteCommand = theCommand;
            final RemoteSystemConnection remoteSystemConnection = theCommand.getRemoteSystemConnection();
            session = prepareSession(theJsch, remoteSystemConnection);
            session.connect();
            channel = session.openChannel("exec");
            final ChannelExec channelExec = (ChannelExec)channel;
            
            channelExec.setCommand(commandString.getBytes(StandardCharsets.UTF_8));
            
            remoteOutput = channelExec.getInputStream();
            remoteError = channelExec.getErrStream();
            localInput = channelExec.getOutputStream();
            channelExec.connect();

            logger.debug("after execution of command {}", commandString);
            
        } catch (final JSchException | IOException e) {
            throw new RemoteCommandFailureException(theCommand,
                                                    e);
        }
    }

    @Override
    public InputStream getCommandOutput() {
        return remoteOutput;
    }

    @Override
    public InputStream getErrorOutput() {
        return remoteError;
    }

    @Override
    public OutputStream getCommandInput() {
        return localInput;
    }

    @Override
    public void close() {
        if (channel.isConnected()) {
            channel.disconnect();
        }
        if (session.isConnected()) {
            session.disconnect();
        }
    }

    @Override
    public ExecReturnCode getExecutionReturnCode() throws NotYetReturnedException {
        final int returnCode = channel.getExitStatus();
        if (returnCode == -1) {
            throw new RemoteNotYetReturnedException(remoteCommand);
        }

        return new ExecReturnCode(returnCode);
    }

    protected Session prepareSession(final JSch aJsch,
                                     final RemoteSystemConnection someConnectionInfo) throws JSchException {
        return aJsch.getSession(someConnectionInfo.getUser(),
                                someConnectionInfo.getHost(),
                                someConnectionInfo.getPort());
    }
}
