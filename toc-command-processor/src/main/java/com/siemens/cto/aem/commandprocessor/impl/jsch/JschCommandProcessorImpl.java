package com.siemens.cto.aem.commandprocessor.impl.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.exec.ExecReturnCode;
import com.siemens.cto.aem.exec.RemoteExecCommand;
import com.siemens.cto.aem.exec.RemoteSystemConnection;
import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import com.siemens.cto.aem.exception.RemoteNotYetReturnedException;

public class JschCommandProcessorImpl implements CommandProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JschCommandProcessorImpl.class);

    private Session session;
    private Channel channel;
    private InputStream remoteOutput;
    private InputStream remoteError;
    private OutputStream localInput;
    private final JSch theJsch;
    private final RemoteExecCommand theCommand;

    public JschCommandProcessorImpl(final JSch theJsch, final RemoteExecCommand theCommand) {
        this.theJsch = theJsch;
        this.theCommand = theCommand;
    }

    public void processCommand() throws RemoteCommandFailureException {

        try {
            logger.debug("before executing command {}", theCommand);

            String commandString = theCommand.getCommand().toCommandString();
            logger.debug("remote Jsch command string is {}", commandString);

            final RemoteSystemConnection remoteSystemConnection = theCommand.getRemoteSystemConnection();

            session = prepareSession(theJsch, remoteSystemConnection);
            session.connect();

            if (theCommand.getCommand().getRunInShell()) {
                channel = session.openChannel("shell");

                final ChannelShell channelShell = (ChannelShell) channel;
                remoteOutput = channelShell.getInputStream();
                remoteError = channelShell.getExtInputStream();
                localInput = channelShell.getOutputStream();
                PrintStream commandStream = new PrintStream(localInput, true);
                channelShell.connect();

                commandStream.println(commandString);
                commandStream.println("exit");
                commandStream.close();
            } else {
                channel = session.openChannel("exec");
                final ChannelExec channelExec = (ChannelExec) channel;

                channelExec.setCommand(commandString.getBytes(StandardCharsets.UTF_8));

                remoteOutput = channelExec.getInputStream();
                remoteError = channelExec.getErrStream();
                localInput = channelExec.getOutputStream();
                channelExec.connect();
            }
            logger.debug("after execution of command {}", commandString);

        } catch (final JSchException | IOException e) {
            throw new RemoteCommandFailureException(theCommand, e);
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
            throw new RemoteNotYetReturnedException(theCommand);
        }

        return new ExecReturnCode(returnCode);
    }

    private Session prepareSession(final JSch aJsch, final RemoteSystemConnection someConnectionInfo)
            throws JSchException {
        final Session session = aJsch.getSession(someConnectionInfo.getUser(), someConnectionInfo.getHost(),
                someConnectionInfo.getPort());
        final String password = someConnectionInfo.getPassword();
        if (password != null) {
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password,gssapi-with-mic,publickey,keyboard-interactive");
        }
        return session;
    }
}
