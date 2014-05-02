package com.siemens.cto.aem.commandprocessor.impl.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.domain.ExecutionReturnCode;
import com.siemens.cto.aem.commandprocessor.domain.NotYetReturnedException;
import com.siemens.cto.aem.commandprocessor.domain.RemoteExecCommand;
import com.siemens.cto.aem.commandprocessor.domain.RemoteNotYetReturnedException;
import com.siemens.cto.aem.commandprocessor.domain.RemoteSystemConnection;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;

public class JschCommandProcessorImpl implements CommandProcessor {

    private final Session session;
    private final Channel channel;
    private final InputStream remoteOutput;
    private final InputStream remoteError;
    private final OutputStream localInput;
    private final RemoteExecCommand remoteCommand;

    public JschCommandProcessorImpl(final JSch theJsch,
                                    final RemoteExecCommand theCommand) throws RemoteCommandFailureException {

        try {
            remoteCommand = theCommand;
            final RemoteSystemConnection remoteSystemConnection = theCommand.getRemoteSystemConnection();
            session = prepareSession(theJsch, remoteSystemConnection);
            session.connect();
            channel = session.openChannel("exec");
            final ChannelExec channelExec = (ChannelExec)channel;
            channelExec.setCommand(theCommand.getCommand().toCommandString().getBytes(StandardCharsets.UTF_8));
            remoteOutput = channelExec.getInputStream();
            remoteError = channelExec.getErrStream();
            localInput = channelExec.getOutputStream();
            channelExec.connect();

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
    public ExecutionReturnCode getExecutionReturnCode() throws NotYetReturnedException {
        final int returnCode = channel.getExitStatus();
        if (returnCode == -1) {
            throw new RemoteNotYetReturnedException(remoteCommand);
        }

        return new ExecutionReturnCode(returnCode);
    }

    protected Session prepareSession(final JSch aJsch,
                                     final RemoteSystemConnection someConnectionInfo) throws JSchException {
        return aJsch.getSession(someConnectionInfo.getUser(),
                                someConnectionInfo.getHost(),
                                someConnectionInfo.getPort());
    }
}
