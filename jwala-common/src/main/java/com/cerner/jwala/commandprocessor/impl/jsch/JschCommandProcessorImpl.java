package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;

public class JschCommandProcessorImpl implements CommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschCommandProcessorImpl.class);
    public static final int CHANNEL_BORROW_LOOP_WAIT_TIME = 180000;

    private final JSch jsch;
    private final JschService jschService;
    private final RemoteExecCommand remoteExecCommand;
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    private ExecReturnCode returnCode;

    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    private static final int SHELL_REMOTE_OUTPUT_READ_WAIT_TIME = 180000;
    private static final int EXEC_REMOTE_OUTPUT_READ_WAIT_TIME = 5000;
    private String commandOutputStr;
    private String errorOutputStr;

    public JschCommandProcessorImpl(final JSch jsch, final RemoteExecCommand remoteExecCommand,
                                    final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool,
                                    final JschService jschService) {
        this.jsch = jsch;
        this.jschService = jschService;
        this.remoteExecCommand = remoteExecCommand;
        this.channelPool = channelPool;
    }

    @Override
    public ExecReturnCode getExecutionReturnCode() {
        return returnCode;
    }

    @Override
    public void processCommand() throws RemoteCommandFailureException {
        if (remoteExecCommand.getCommand().getRunInShell()) {
            processShellCommand();
        } else {
            processExecCommand();
        }
    }

    /**
     * Process a shell command.
     */
    public void processShellCommand() {
        ChannelShell channel = null;
        ChannelSessionKey channelSessionKey = new ChannelSessionKey(remoteExecCommand.getRemoteSystemConnection(), ChannelType.SHELL);
        LOGGER.debug("channel session key = {}", channelSessionKey);
        try {
            channel = getChannelShell(channelSessionKey, CHANNEL_BORROW_LOOP_WAIT_TIME);

            final RemoteCommandReturnInfo remoteCommandReturnInfo = jschService.runCommand(
                    remoteExecCommand.getCommand().toCommandString(), channel, SHELL_REMOTE_OUTPUT_READ_WAIT_TIME);

            commandOutputStr = remoteCommandReturnInfo.standardOuput;
            returnCode = new ExecReturnCode(remoteCommandReturnInfo.retCode);
        } catch (final IOException | JSchException e) {
            final String errMsg = MessageFormat.format("Failed to process shell command: {0}!", remoteExecCommand);
            LOGGER.error(errMsg, e);
            throw new JschCommandProcessorImplException(errMsg, e);
        } finally {
            if (channel != null) {
                channelPool.returnObject(channelSessionKey, channel);
                LOGGER.debug("channel {} returned", channel.getId());
            }
        }
    }

    /**
     * Acquires a channel
     * @param channelSessionKey the session key that is used to acquire a channel
     * @param timeout the given time limit to acquire a channel, if reached a {@link JschCommandProcessorImplException} is thrown
     * @return {@link ChannelShell}
     */
    private ChannelShell getChannelShell(final ChannelSessionKey channelSessionKey, final long timeout) {
        ChannelShell channel = null;
        final long startTime = System.currentTimeMillis();
        while (channel == null || !channel.isConnected()) {
            if ((System.currentTimeMillis() - startTime) > timeout) {
                throw new JschCommandProcessorImplException("Channel acquisition timeout!");
            }

            try {
                LOGGER.debug("borrowing a channel...");
                channel = (ChannelShell) channelPool.borrowObject(channelSessionKey);
                LOGGER.debug("channel {} borrowed", channel.getId());

                if (!channel.isConnected()) {
                    try {
                        LOGGER.debug("channel {} connecting...");
                        channel.connect(CHANNEL_CONNECT_TIMEOUT);
                        LOGGER.debug("channel {} connected!", channel.getId());
                    } catch (final JSchException jsche) {
                        LOGGER.error("Borrowed channel {} connection failed! Invalidating the channel!", channel.getId(), jsche);
                        channelPool.invalidateObject(channelSessionKey, channel);
                    }
                } else {
                    LOGGER.debug("Channel {} already connected!", channel.getId());
                }
            } catch (final Exception e) {
                throw new JschCommandProcessorImplException("Failed to get channel!", e);
            }
        }
        return channel;
    }

    /**
     * Process and exec command.
     */
    public void processExecCommand() {
        Session session = null;
        ChannelExec channel = null;
        try {
            // We can't keep the session and the channels open for type exec since we need the exit code and the
            // standard error e.g. thread dump uses this and requires the exit code and the standard error.
            LOGGER.debug("preparing session...");
            session = jschService.prepareSession(remoteExecCommand.getRemoteSystemConnection());
            session.connect();
            LOGGER.debug("session connected");
            channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getChannelType());

            final RemoteCommandReturnInfo remoteCommandReturnInfo = jschService.runCommand(
                    remoteExecCommand.getCommand().toCommandString(), channel, EXEC_REMOTE_OUTPUT_READ_WAIT_TIME);

            returnCode = new ExecReturnCode(remoteCommandReturnInfo.retCode);
            commandOutputStr = remoteCommandReturnInfo.standardOuput;
            errorOutputStr = remoteCommandReturnInfo.errorOupout;
        } catch (final JSchException | IOException e) {
            LOGGER.error("Error processing exec command!", e);
            returnCode = new ExecReturnCode(-1);
            errorOutputStr = e.getMessage();
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
                LOGGER.debug("Channel {} disconnected!", channel.getId());
            }

            if (session != null && session.isConnected()) {
                session.disconnect();
                LOGGER.debug("session disconnected");
            }
        }
    }

    @Override
    public String getCommandOutputStr() {
        return commandOutputStr;
    }

    @Override
    public String getErrorOutputStr() {
        return errorOutputStr;
    }

    @Override
    public void close() throws IOException {}

}
