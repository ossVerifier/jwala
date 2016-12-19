package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.exception.ExitCodeNotAvailableException;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JschCommandProcessorImpl implements CommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschCommandProcessorImpl.class);
    public static final int CHANNEL_BORROW_LOOP_WAIT_TIME = 180000;

    private final JSch jsch;
    private final RemoteExecCommand remoteExecCommand;
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    private ExecReturnCode returnCode;

    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    private static final int REMOTE_OUTPUT_STREAM_MAX_WAIT_TIME = 180000;
    private static final String EXIT_CODE_START_MARKER = "EXIT_CODE";
    private static final String EXIT_CODE_END_MARKER = "***";
    private String commandOutputStr;
    private String errorOutputStr;

    public JschCommandProcessorImpl(final JSch jsch, final RemoteExecCommand remoteExecCommand,
                                    final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool) {
        this.jsch = jsch;
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

            final InputStream in = channel.getInputStream();
            final OutputStream out = channel.getOutputStream();

            final String commandString = remoteExecCommand.getCommand().toCommandString();
            out.write(commandString.getBytes(StandardCharsets.UTF_8));
            LOGGER.debug("commandString = {}", commandString);
            out.write("echo 'EXIT_CODE='$?***".getBytes(StandardCharsets.UTF_8));
            out.write("echo -n -e '\\xff'".getBytes(StandardCharsets.UTF_8));
            out.flush();

            commandOutputStr = readRemoteOutput(in);
            LOGGER.debug("commandOutput=" + commandOutputStr);
            returnCode = parseReturnCode(commandOutputStr);
            LOGGER.debug("return code =" + returnCode);
        } catch (final IOException e) {
            LOGGER.error("Failed to process shell command: {}!", remoteExecCommand, e);
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
            session = prepareSession(remoteExecCommand.getRemoteSystemConnection());
            session.connect();
            LOGGER.debug("session connected");
            channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getChannelType());
            LOGGER.debug("executing command: {}", remoteExecCommand.getCommand().toCommandString());
            channel.setCommand(remoteExecCommand.getCommand().toCommandString().getBytes(StandardCharsets.UTF_8));

            final InputStream remoteOutput = channel.getInputStream();
            final InputStream remoteError = channel.getErrStream();

            LOGGER.debug("channel {} connecting...", channel.getId());
            channel.connect(CHANNEL_CONNECT_TIMEOUT);
            LOGGER.debug("channel {} connected!", channel.getId());

            LOGGER.debug("reading remote output...");
            final StringBuilder remoteOutputStringBuilder = new StringBuilder();
            final StringBuilder remoteErrorStringBuilder = new StringBuilder();
            while (!channel.isClosed()) {
                remoteOutputStringBuilder.append((char) remoteOutput.read());
            }

            if (channel.getExitStatus() != 0) {
                int readByte = remoteError.read();
                while (readByte != -1) {
                    remoteErrorStringBuilder.append((char) readByte);
                    readByte = remoteError.read();
                }
            }

            returnCode = new ExecReturnCode(channel.getExitStatus());
            LOGGER.debug("exit code = {}", returnCode.getReturnCode());

            commandOutputStr = remoteOutputStringBuilder.toString();
            LOGGER.debug("remote output = {}", commandOutputStr);

            errorOutputStr = remoteErrorStringBuilder.toString();
            LOGGER.debug("remote error = {}", errorOutputStr);

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

    /**
     * Read remote output stream.
     * @param in the input stream
     * @throws IOException
     */
    protected String readRemoteOutput(final InputStream in) throws IOException {
        boolean timeout = false;
        int readByte = in.read();
        LOGGER.debug("reading remote output...");
        final StringBuilder inStringBuilder = new StringBuilder();

        final long startTime = System.currentTimeMillis();
        while(readByte != 0xff) {
            if ((System.currentTimeMillis() - startTime) > REMOTE_OUTPUT_STREAM_MAX_WAIT_TIME) {
                timeout = true;
                break;
            }
            inStringBuilder.append((char)readByte);
            readByte = in.read();
        }

        if (timeout) {
            LOGGER.warn("remote output reading timeout!");
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
        return inStringBuilder.toString();
    }

    /**
     * Parse the return code from the output string.
     * @param outputStr the output string
     * @return {@link ExecReturnCode}
     */
    protected ExecReturnCode parseReturnCode(final String outputStr) {
        if (outputStr != null) {
            final String exitCodeStr = outputStr.substring(outputStr.lastIndexOf(EXIT_CODE_START_MARKER)
                    + EXIT_CODE_START_MARKER.length() + 1, outputStr.lastIndexOf(EXIT_CODE_END_MARKER));
            return new ExecReturnCode(Integer.parseInt(exitCodeStr));
        }
        throw new ExitCodeNotAvailableException(remoteExecCommand.getCommand().toCommandString());
    }

    /**
     * Prepare the session by setting session properties.
     * @param remoteSystemConnection contains connection details use to prepare a session
     * @return {@link Session}
     * @throws JSchException
     */
    private Session prepareSession(final RemoteSystemConnection remoteSystemConnection)  throws JSchException {
        final Session session = jsch.getSession(remoteSystemConnection.getUser(), remoteSystemConnection.getHost(),
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
