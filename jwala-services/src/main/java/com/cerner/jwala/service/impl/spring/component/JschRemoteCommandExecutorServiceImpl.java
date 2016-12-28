package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.jcraft.jsch.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Implementation of {@link RemoteCommandExecutorService} using JSCH.
 * <p/>
 * Created by Jedd Cuison on 3/25/2016.
 */
@Service
public class JschRemoteCommandExecutorServiceImpl implements RemoteCommandExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschRemoteCommandExecutorServiceImpl.class);
    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    private static final int CHANNEL_BORROW_LOOP_WAIT_TIME = 180000;
    private static final int SHELL_REMOTE_OUTPUT_READ_WAIT_TIME = 180000;
    private static final int EXEC_REMOTE_OUTPUT_READ_WAIT_TIME = 3000;

    private final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;
    private final JschService jschService;

    @Autowired
    public JschRemoteCommandExecutorServiceImpl(final JSch jSch,
                                                final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool,
                                                final JschService jschService) {
        this.channelPool = channelPool;
        this.jschService = jschService;
    }

    @Override
    public RemoteCommandReturnInfo executeCommand(final RemoteExecCommand remoteExecCommand) {
        if (remoteExecCommand.getCommand().getRunInShell()) {
            return executeShellCommand(remoteExecCommand);
        }
        return executeExecCommand(remoteExecCommand);
    }

    /**
     * Execute a command via shell.
     *
     * @param remoteExecCommand wrapper that contains command details
     * @return {@link RemoteCommandReturnInfo}
     */
    protected RemoteCommandReturnInfo executeShellCommand(final RemoteExecCommand remoteExecCommand) {
        ChannelShell channel = null;
        ChannelSessionKey channelSessionKey = new ChannelSessionKey(remoteExecCommand.getRemoteSystemConnection(), ChannelType.SHELL);
        LOGGER.debug("channel session key = {}", channelSessionKey);

        try {

            try {
                channel = getChannelShell(channelSessionKey);
            } catch (final Exception e) {
                throw new RemoteCommandExecutorServiceException("Failed to get channel!", e);
            }

            return jschService.runCommand(remoteExecCommand.getCommand().toCommandString(), channel,
                                          SHELL_REMOTE_OUTPUT_READ_WAIT_TIME);
        } catch (final IOException | JSchException e) {
            throw new RemoteCommandExecutorServiceException(e);
        }  finally {
            if (channel != null) {
                channelPool.returnObject(channelSessionKey, channel);
                LOGGER.debug("channel {} returned", channel.getId());
            }
        }
    }

    /**
     * Get a {@link ChannelShell}
     * @param channelSessionKey the session key that identifies the channel
     * @return {@link ChannelShell}
     * @throws Exception thrown by borrowObject and invalidateObject
     */
    private ChannelShell getChannelShell(final ChannelSessionKey channelSessionKey) throws Exception {
        final long startTime = System.currentTimeMillis();
        Channel channel;
        do {
            LOGGER.debug("borrowing a channel...");
            channel = channelPool.borrowObject(channelSessionKey);
            if (channel != null) {
                LOGGER.debug("channel {} borrowed", channel.getId());
                if (!channel.isConnected()) {
                    try {
                        LOGGER.debug("channel {} connecting...", channel.getId());
                        channel.connect(CHANNEL_CONNECT_TIMEOUT);
                        LOGGER.debug("channel {} connected!", channel.getId());
                    } catch (final JSchException jsche) {
                        LOGGER.error("Borrowed channel {} connection failed! Invalidating the channel...",
                                channel.getId(), jsche);
                        channelPool.invalidateObject(channelSessionKey, channel);
                    }
                } else {
                    LOGGER.debug("Channel {} already connected!", channel.getId());
                }
            }

            if ((channel == null || !channel.isConnected()) && (System.currentTimeMillis() - startTime) > CHANNEL_BORROW_LOOP_WAIT_TIME) {
                final String errMsg = MessageFormat.format("Failed to get a channel within {0} ms! Aborting channel acquisition!",
                        CHANNEL_BORROW_LOOP_WAIT_TIME);
                LOGGER.error(errMsg);
                throw new RemoteCommandExecutorServiceException(errMsg);
            }
        } while (channel == null || !channel.isConnected());
        return (ChannelShell) channel;
    }

    /**
     * Execute a command directly (do not open a shell) then exit immediately
     *
     * @param remoteExecCommand wrapper that contains command details
     * @return {@link RemoteCommandReturnInfo}
     */
    protected RemoteCommandReturnInfo executeExecCommand(final RemoteExecCommand remoteExecCommand) {
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

            LOGGER.debug("Executing remote cmd {} ", remoteExecCommand.getCommand().toCommandString());
            return jschService.runCommand(remoteExecCommand.getCommand().toCommandString(), channel,
                    EXEC_REMOTE_OUTPUT_READ_WAIT_TIME);
        } catch (final JSchException | IOException e) {
            LOGGER.error("Error processing exec command!", e);
            return new RemoteCommandReturnInfo(-1, null, e.getMessage());
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

}
