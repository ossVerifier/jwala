package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.InternalErrorException;
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
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
    private static final int REMOTE_OUTPUT_STREAM_MAX_WAIT_TIME = 180000;
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

        InputStream in = null;
        OutputStream out = null;

        final long startTime = System.currentTimeMillis();

        try {
            while ((channel == null || !channel.isConnected()) &&
                    (System.currentTimeMillis() - startTime) < CHANNEL_BORROW_LOOP_WAIT_TIME) {
                LOGGER.debug("borrowing a channel...");
                channel = (ChannelShell) channelPool.borrowObject(channelSessionKey);
                if (channel != null) {
                    LOGGER.debug("channel {} borrowed", channel.getId());
                    in = channel.getInputStream();
                    out = channel.getOutputStream();
                    if (!channel.isConnected()) {
                        try {
                            LOGGER.debug("channel {} connecting...");
                            channel.connect(CHANNEL_CONNECT_TIMEOUT);
                            LOGGER.debug("channel {} connected!", channel.getId());
                        } catch (final JSchException jsche) {
                            LOGGER.error("Borrowed channel {} connection failed! Invalidating the channel!",
                                    channel.getId(), jsche);
                            channelPool.invalidateObject(channelSessionKey, channel);
                        }
                    } else {
                        LOGGER.debug("Channel {} already connected!", channel.getId());
                    }
                }
            }

            // Still no channel to borrow ? Let's just give up and throw in the towel!
            if (channel == null) {
                final RemoteCommandExecutorServiceException e =
                        new RemoteCommandExecutorServiceException("Was not able to borrow a channel!");
                LOGGER.error(e.getMessage());
                throw e;
            }

            final PrintStream commandStream = new PrintStream(out, true);
            commandStream.println(remoteExecCommand.getCommand().toCommandString());
            commandStream.println("echo 'EXIT_CODE='$?***");
            commandStream.println("echo -n -e '\\xff'");

            String commandOutputStr = readRemoteOutput(in, channel, channelSessionKey);
            int retCode = parseReturnCode(commandOutputStr, remoteExecCommand).getReturnCode();
            return new RemoteCommandReturnInfo(retCode, commandOutputStr, StringUtils.EMPTY);
        } catch (final Exception e) {
            throw new RemoteCommandExecutorServiceException(e);
        } finally {
            if (channel != null) {
                channelPool.returnObject(channelSessionKey, channel);
                LOGGER.debug("channel {} returned", channel.getId());
            }
        }
    }

    /**
     * Execute a command directly (do not open a shell) then exit immediately.
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
            channel.setCommand(remoteExecCommand.getCommand().toCommandString().getBytes(StandardCharsets.UTF_8));

            final InputStream remoteOutput = channel.getInputStream();
            final InputStream remoteError = channel.getErrStream();

            LOGGER.debug("channel {} connecting...", channel.getId());
            channel.connect(CHANNEL_CONNECT_TIMEOUT);
            LOGGER.debug("channel {} connected!", channel.getId());

            LOGGER.debug("reading remote output...");
            final StringBuilder remoteOutputStringBuilder = new StringBuilder();
            final StringBuilder remoteErrorStringBuilder = new StringBuilder();

            int readByte;

            // read output
            do {
                readByte = remoteOutput.read();
                if (readByte != -1) {
                    remoteOutputStringBuilder.append((char) readByte);
                }
            } while (!channel.isClosed() && readByte != -1);

            // read error
            if (channel.getExitStatus() != 0) {
                readByte = remoteError.read();
                while (readByte != -1) {
                    remoteErrorStringBuilder.append((char) readByte);
                    readByte = remoteError.read();
                }
            }

            retCode = new ExecReturnCode(channel.getExitStatus());
            LOGGER.debug("exit code = {}", retCode.getReturnCode());

            commandOutputStr = remoteOutputStringBuilder.toString();
            LOGGER.debug("remote output = {}", commandOutputStr);

            errorOutputStr = remoteErrorStringBuilder.toString();
            LOGGER.debug("remote error = {}", errorOutputStr);

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
     * Read remote output stream.
     *
     * @param in                the input stream
     * @param channel
     * @param channelSessionKey
     * @throws IOException
     */
    protected String readRemoteOutput(InputStream in, final ChannelShell channel, ChannelSessionKey channelSessionKey) throws Exception {
        boolean timeout = false;
        int readByte = in.read();
        LOGGER.debug("Reading remote output ...");
        StringBuilder inStringBuilder = new StringBuilder();

        final long startTime = System.currentTimeMillis();
        while (readByte != 0xff) {
            if ((System.currentTimeMillis() - startTime) > REMOTE_OUTPUT_STREAM_MAX_WAIT_TIME) {
                timeout = true;
                break;
            }
            if (readByte != -1) {
                inStringBuilder.append((char) readByte);
            } else {
                LOGGER.debug("Reached the end of file reading the stream");
                LOGGER.debug("Channel is closed");
                LOGGER.debug("Channel exit status {}", channel.getExitStatus());
                LOGGER.debug("Channel being returned to the pool");
                channelPool.returnObject(channelSessionKey, channel);
                throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, "Input stream to channel ended before return value received");
            }
            int length = inStringBuilder.length();
            if (length > 16384) {
                LOGGER.error("OOM found large string of length {} :: {}", length, inStringBuilder.toString());
                inStringBuilder.append(EXIT_CODE_START_MARKER);
                inStringBuilder.append("=1");
                inStringBuilder.append(EXIT_CODE_END_MARKER);
                String result = inStringBuilder.toString();
                inStringBuilder = null;
                return result;
            }

            // TODO: Find a way how to timeout from Inputstream read. The timeout mechanism above may not work when read is blocking.
            readByte = in.read();

            if (readByte == -1) {
                LOGGER.error("Read -1 from shell stream - closing connection for unexpected output");
            } else if (readByte == 255) {
                LOGGER.debug("Received expected value of 0xff (255) from shell stream - done processing command");
            }
        }

        if (timeout) {
            LOGGER.error("remote output reading timeout!");
            // Don't throw an exception here like it was suggested before since this simply means that there's no 'EOL'
            // char coming in from the stream so as such just don't do anything with the status. If the status is
            // in the "ING" state e.g. stopping, then let it hang there. If ever we really want to throw an error
            // It has to be a timeout error and it shouldn't interfere on how the UI functions (like the states should
            // still be displayed not missing in the case of just throwing and error from here)
        } else {
            LOGGER.debug("****** output: start ******");
            LOGGER.debug(inStringBuilder.toString());
            LOGGER.debug("****** output: end ******");
        }

        String result = inStringBuilder.toString();
        inStringBuilder = null;
        return result;
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
        final String password = remoteSystemConnection.getPassword();
        if (password != null) {
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password,gssapi-with-mic,publickey,keyboard-interactive");
        }
        return session;
    }
}
