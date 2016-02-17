package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.*;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.exception.ExitCodeNotAvailableException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JschCommandProcessorImpl implements CommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschCommandProcessorImpl.class);
    public static final int CHANNEL_EXIT_WAIT_TIMEOUT = 600000;
    public static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    public static final int THREAD_SLEEP_TIME = 500;
    public static final String EXIT_CODE_START_MARKER = "EXIT_CODE";
    public static final String EXIT_CODE_END_MARKER = "***";

    protected Session session;
    protected Channel channel;
    protected InputStream remoteOutput;
    private StringBuilder remoteOutputStringBuilder;
    protected InputStream remoteError;
    private StringBuilder remoteErrorStringBuilder;
    protected OutputStream localInput;

    final JSch theJsch;
    final RemoteExecCommand theCommand;

    private static final Map<String, Object> COMMAND_MAP = new HashMap<>();

    public JschCommandProcessorImpl(final JSch theJsch, final RemoteExecCommand theCommand) {
        this.theJsch = theJsch;
        this.theCommand = theCommand;
    }

    public void processCommand() throws RemoteCommandFailureException {
        // Check if the command is already being processed, if it is don't do anything.
        synchronized (COMMAND_MAP) {
            if (!COMMAND_MAP.containsKey(theCommand.getCommand().toCommandString())) {
                COMMAND_MAP.put(theCommand.getCommand().toCommandString(), null);
            } else {
                LOGGER.warn("The command '{}' is already being processed!", theCommand.getCommand().toCommandString());
                return;
            }
        }

        final RemoteSystemConnection remoteSystemConnection = theCommand.getRemoteSystemConnection();
        final String channelType = theCommand.getCommand().getRunInShell() ? "shell" : "exec";
        try {
            LOGGER.debug("before executing command {}", theCommand);

            String commandString = theCommand.getCommand().toCommandString();
            LOGGER.debug("remote Jsch command string is {}", commandString);
            if (theCommand.getCommand().getRunInShell()) {
                borrowChannel(remoteSystemConnection, channelType);
                final ChannelShell channelShell = (ChannelShell) channel;

                remoteOutput = channelShell.getInputStream();
                remoteError = channelShell.getExtInputStream();
                localInput = channelShell.getOutputStream();

                LOGGER.info("Channel {} isConnected = {}", channel.getId(), channel.isConnected());
                if (!channel.isConnected()) {
                    LOGGER.info("Session isConnected = {}; Channel with id = {} connecting...", channel.getSession().isConnected(), channel.getId());
                    channel.connect(CHANNEL_CONNECT_TIMEOUT); // This should always come after getting the streams.
                }

                LOGGER.info("Channel with id = {} connected!", channel.getId() );

                PrintStream commandStream = new PrintStream(localInput, true);
                commandStream.println(commandString);
                commandStream.println("echo 'EXIT_CODE='$?***");
                commandStream.println("echo -n -e '\\xff'");

                readRemoteOutput();

                LOGGER.info("Channel with id = {} exit status = {}; isConnected = {}; isClosed = {}.", channel.getId(),
                        channel.getExitStatus(), channel.isConnected(), channel.isClosed());
            } else {
                // We can't keep the session and the channels open for type exec since we need the exit code and the
                // standard error e.g. thread dump uses this and requires the exit code and the standard error.
                LOGGER.info("preparing session...");
                session = prepareSession(theJsch, remoteSystemConnection);
                session.connect();
                LOGGER.info("session connected");
                channel = session.openChannel("exec");
                final ChannelExec channelExec = (ChannelExec) channel;

                channelExec.setCommand(commandString.getBytes(StandardCharsets.UTF_8));

                remoteOutput = channelExec.getInputStream();
                remoteError = channelExec.getErrStream();
                localInput = channelExec.getOutputStream();

                LOGGER.info("channel connecting...");
                channelExec.connect(CHANNEL_CONNECT_TIMEOUT);
                LOGGER.info("reading remote output...");

                remoteOutputStringBuilder = new StringBuilder();
                remoteErrorStringBuilder = new StringBuilder();
                while (!channelExec.isClosed()) {
                    remoteOutputStringBuilder.append((char) remoteOutput.read());
                }

                if (channel.getExitStatus() != 0) {
                    int readByte = remoteError.read();
                    while (readByte != -1) {
                        remoteErrorStringBuilder.append((char) readByte);
                        readByte = remoteError.read();
                    }
                }

                LOGGER.info("exit status = {}", channel.getExitStatus());
                LOGGER.info("remote output = {}", remoteOutputStringBuilder.toString());
                LOGGER.info("remote error = {}", remoteErrorStringBuilder.toString());
                session.disconnect();
                LOGGER.info("session disconnected");
            }


        } catch (final JSchException | IOException e) {
            LOGGER.error("Command '{}' had an error: {} !", theCommand.getCommand().toCommandString(), e.getMessage());
            throw new RemoteCommandFailureException(theCommand, e);
        } finally {
            if (theCommand.getCommand().getRunInShell()) {
                JschChannelManager.getInstance().returnChannel(remoteSystemConnection.getHost(), channel, channelType);
            }

            synchronized (COMMAND_MAP) {
                COMMAND_MAP.remove(theCommand.getCommand().toCommandString());
            }
        }
    }

    /**
     * Borrow a channel, wait if nothing's available.
     * @param remoteSystemConnection contains connection information
     * @param channelType e.g. shell or exec
     * @throws JSchException
     * @throws RemoteCommandFailureException
     */
    private void borrowChannel(RemoteSystemConnection remoteSystemConnection, String channelType) throws JSchException,
            RemoteCommandFailureException {
        final long startTime = System.currentTimeMillis();
        while (channel == null) {
            channel = JschChannelManager.getInstance().getChannel(theJsch, remoteSystemConnection, channelType);
            try {
                LOGGER.info("Command '{}' is waiting for a channel...", theCommand.getCommand().toCommandString());
                if ((System.currentTimeMillis() - startTime) > 600000) {
                    throw new RemoteCommandFailureException(theCommand, new RuntimeException("Timeout reached waiting for a channel!"));
                }
                Thread.sleep(THREAD_SLEEP_TIME);
            } catch (final InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.error("Cannot acquire channel!");
                throw new RemoteCommandFailureException(theCommand, e);
            }
        }
        LOGGER.info("Command '{}' now has a channel!", theCommand.getCommand().toCommandString());
    }

    /**
     * Read remote output stream.
     * @throws IOException
     */
    private void readRemoteOutput() throws IOException {
        final long startTime = System.currentTimeMillis();
        boolean timeout = false;
        int readByte = remoteOutput.read();
        LOGGER.info("reading remote output...");
        remoteOutputStringBuilder = new StringBuilder();
        while(readByte != 0xff) {
            if ((System.currentTimeMillis() - startTime) >= CHANNEL_EXIT_WAIT_TIMEOUT) {
                timeout = true;
                break;
            }
            remoteOutputStringBuilder.append((char)readByte);
            readByte = remoteOutput.read();
        }

        if (timeout) {
            LOGGER.error("remote output reading timeout!");
        } else {
            LOGGER.info("done streaming remote output, exit code = {}", getExecutionReturnCode().getReturnCode());
            LOGGER.info("****** output: start ******");
            LOGGER.info(remoteOutputStringBuilder.toString());
            LOGGER.info("****** output: end ******");
        }
    }

    @Override
    public String getCommandOutputStr() {
        return remoteOutputStringBuilder == null ? "" : remoteOutputStringBuilder.toString();
    }

    @Override
    public String getErrorOutputStr() {
        return remoteErrorStringBuilder == null ? "" : remoteErrorStringBuilder.toString();
    }

    @Override
    public void close() {}

    @Override
    public ExecReturnCode getExecutionReturnCode() {
        if (theCommand.getCommand().getRunInShell()) {
            if (remoteOutputStringBuilder != null) {
                final String remoteOutputStr = remoteOutputStringBuilder.toString();
                 final String exitCodeStr = remoteOutputStr.substring(remoteOutputStr.lastIndexOf(EXIT_CODE_START_MARKER)
                         + EXIT_CODE_START_MARKER.length() + 1, remoteOutputStr.lastIndexOf(EXIT_CODE_END_MARKER));
                return new ExecReturnCode(Integer.parseInt(exitCodeStr));
            }
            throw new ExitCodeNotAvailableException(theCommand.getCommand().toCommandString());
        }

        final int returnCode = channel.getExitStatus();
        if (returnCode == -1) {
            throw new ExitCodeNotAvailableException(theCommand.getCommand().toCommandString());
        }

        return new ExecReturnCode(returnCode);
    }

    Session prepareSession(final JSch aJsch, final RemoteSystemConnection someConnectionInfo) throws JSchException {
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
