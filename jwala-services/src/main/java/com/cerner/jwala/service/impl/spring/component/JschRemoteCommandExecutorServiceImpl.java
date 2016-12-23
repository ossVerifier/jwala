package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.exception.ExitCodeNotAvailableException;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.exception.RemoteCommandExecutorServiceException;
import com.jcraft.jsch.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
    private static final String EXIT_CODE_START_MARKER = "EXIT_CODE";
    private static final String EXIT_CODE_END_MARKER = "***";

    private final JSch jSch;
    private final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Autowired
    public JschRemoteCommandExecutorServiceImpl(final JSch jSch,
                                                final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool) {
        this.jSch = jSch;
        this.channelPool = channelPool;
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

        InputStream in;
        OutputStream out;

        try {

            try {
                channel = getChannelShell(channelSessionKey);
            } catch (final Exception e) {
                throw new RemoteCommandExecutorServiceException("Failed to get channel!", e);
            }

            in = channel.getInputStream();
            out = channel.getOutputStream();

            out.write(remoteExecCommand.getCommand().toCommandString().getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("echo 'EXIT_CODE='$?***".getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            out.write("echo -n -e '\\xff'".getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            LOGGER.debug("Reading remote output ...");
            final String commandOutputStr = readRemoteOutput(in, (char) 0xff, SHELL_REMOTE_OUTPUT_READ_WAIT_TIME);
            LOGGER.debug("****** output: start ******");
            LOGGER.debug(commandOutputStr);
            LOGGER.debug("****** output: end ******");

            int retCode = parseReturnCode(commandOutputStr, remoteExecCommand).getReturnCode();
            return new RemoteCommandReturnInfo(retCode, commandOutputStr, StringUtils.EMPTY);
        } catch (final IOException e) {
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
        ExecReturnCode retCode;
        String commandOutputStr = null;
        String errorOutputStr = null;
        try {
            // We can't keep the session and the channels open for type exec since we need the exit code and the
            // standard error e.g. thread dump uses this and requires the exit code and the standard error.
            LOGGER.debug("preparing session...");
            session = prepareSession(remoteExecCommand.getRemoteSystemConnection());
            session.connect();
            LOGGER.debug("session connected");
            channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getChannelType());
            LOGGER.debug("Executing remote cmd {} ", remoteExecCommand.getCommand().toCommandString());
            channel.setCommand(remoteExecCommand.getCommand().toCommandString().getBytes(StandardCharsets.UTF_8));

            final InputStream remoteOutput = channel.getInputStream();
            final InputStream remoteError = channel.getErrStream();

            LOGGER.debug("channel {} connecting...", channel.getId());
            channel.connect(CHANNEL_CONNECT_TIMEOUT);
            LOGGER.debug("channel {} connected!", channel.getId());

            LOGGER.debug("reading remote output...");
            commandOutputStr = readRemoteOutput(remoteOutput, null, EXEC_REMOTE_OUTPUT_READ_WAIT_TIME);
            LOGGER.debug("remote output = {}", commandOutputStr);

            if (channel.getExitStatus() != 0) {
                errorOutputStr = readRemoteOutput(remoteError, null, EXEC_REMOTE_OUTPUT_READ_WAIT_TIME);
                LOGGER.debug("remote error = {}", errorOutputStr);
            }

            retCode = new ExecReturnCode(channel.getExitStatus());
            LOGGER.debug("exit code = {}", retCode.getReturnCode());
        } catch (final JSchException | IOException e) {
            LOGGER.error("Error processing exec command!", e);
            retCode = new ExecReturnCode(-1);
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

        return new RemoteCommandReturnInfo(retCode.getReturnCode(), commandOutputStr, errorOutputStr);
    }

    /**
     * Reads data streamed from a remote connection
     * @param remoteOutput the inputstream where the remote connection will stream data to
     * @param dataEndMarker a marker which tells the method to stop reading from the inputstream. If this is null
     *            then the method will try to read data from the input stream until read timeout is reached.
     * @param timeout the length of time in which to wait for incoming data from the stream
     * @return the data streamed from the remote connection
     * @throws IOException
     */
    private String readRemoteOutput(final InputStream remoteOutput, final Character dataEndMarker, final long timeout) throws IOException {
        final StringBuilder remoteOutputStringBuilder = new StringBuilder();
        long startTime = System.currentTimeMillis();
        while (true) {
            if (remoteOutput.available() != 0) {
                char readChar = (char) remoteOutput.read();
                remoteOutputStringBuilder.append(readChar);
                startTime = System.currentTimeMillis();

                if (dataEndMarker != null && readChar == dataEndMarker) {
                    LOGGER.debug("Read EOF character '{}', stopping remote output reading...", readChar);
                    return remoteOutputStringBuilder.toString();
                }
            }

            if ((System.currentTimeMillis() - startTime) > timeout) {
                LOGGER.warn("Remote output reading timeout!");
                break;
            }
        }
        return remoteOutputStringBuilder.toString();
    }

    /**
     * Parse the return code from the output string.
     *
     * @param outputStr the output string
     * @return {@link ExecReturnCode}
     */
    protected ExecReturnCode parseReturnCode(final String outputStr, final RemoteExecCommand remoteExecCommand) {
        if (outputStr != null) {
            final String exitCodeStr = outputStr.substring(outputStr.lastIndexOf(EXIT_CODE_START_MARKER)
                    + EXIT_CODE_START_MARKER.length() + 1, outputStr.lastIndexOf(EXIT_CODE_END_MARKER));
            return new ExecReturnCode(Integer.parseInt(exitCodeStr));
        }
        throw new ExitCodeNotAvailableException(remoteExecCommand.getCommand().toCommandString());
    }

    /**
     * Prepare the session by setting session properties.
     *
     * @param remoteSystemConnection {@link RemoteSystemConnection}
     * @return {@link Session}
     * @throws JSchException
     */
    protected Session prepareSession(final RemoteSystemConnection remoteSystemConnection) throws JSchException {
        final Session session = jSch.getSession(remoteSystemConnection.getUser(), remoteSystemConnection.getHost(),
                remoteSystemConnection.getPort());
        final char[] encryptedPassword = remoteSystemConnection.getEncryptedPassword();
        if (encryptedPassword != null) {
            session.setPassword(new DecryptPassword().decrypt(encryptedPassword));
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password,gssapi-with-mic,publickey,keyboard-interactive");
        }
        return session;
    }
}
