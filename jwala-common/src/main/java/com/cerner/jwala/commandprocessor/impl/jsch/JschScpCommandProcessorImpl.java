package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

public class JschScpCommandProcessorImpl implements CommandProcessor {
    private static final int TIMEOUT = 300000;
    public static final String JSCH_READ_ACK_SLEEP_DURATION = "jsch.read.ack.sleep.duration";
    public static final String READ_ACK_SLEEP_DEFAULT_VALUE = "250";
    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    private static final int CHANNEL_BORROW_LOOP_WAIT_TIME = 180000;
    private boolean checkAckOk;
    private static final Logger LOGGER = LoggerFactory.getLogger(JschScpCommandProcessorImpl.class);

    private final JSch jsch;
    private final RemoteExecCommand remoteCommand;

    private OutputStream localInput;
    private InputStream remoteOutput;

    private final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    public JschScpCommandProcessorImpl(JSch jsch, RemoteExecCommand remoteCommand, GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool) {
        this.jsch = jsch;
        this.remoteCommand = remoteCommand;
        this.channelPool = channelPool;
    }

    /**
     * Taken from the Jsch example ScpTo.java
     *
     * @throws RemoteCommandFailureException
     */
    @Override
    public void processCommand() throws RemoteCommandFailureException {
        checkAckOk = false;

        final List<String> commandFragments = remoteCommand.getCommand().getCommandFragments();
        Session session = null;
        Channel channel = null;

        final RemoteSystemConnection remoteSystemConnection = remoteCommand.getRemoteSystemConnection();
        final ChannelSessionKey channelSessionKey = new ChannelSessionKey(remoteSystemConnection, ChannelType.EXEC);
        ChannelExec channelExec = null;

        try {
            LOGGER.debug("scp remote command {} source:{} destination:{}", remoteSystemConnection, commandFragments.get(1), commandFragments.get(2));

//            session = prepareSession(remoteSystemConnection);
//            session.connect();

            final String target = commandFragments.get(2).replace("\\", "/");
            // exec 'scp -t rfile' remotely
            final String command = "scp -t " + target;

//            channel = session.openChannel("exec");

            channel = getChannelExec(channelSessionKey);

            channelExec = (ChannelExec) channel;
            channelExec.setCommand(command);

            // get I/O streams for remote scp
            localInput = channelExec.getOutputStream();
            remoteOutput = channelExec.getInputStream();

            channelExec.connect();

            LOGGER.debug(">>>>>> Channel connected...");
            if (checkAck(remoteOutput) != 0) {
                throw new RemoteCommandFailureException(remoteCommand, new Throwable("Failed to connect to the remote host during secure copy"));
            }

            final String filePath = commandFragments.get(1);
            sendFileInfo(filePath);
            sendFileContent(filePath);
        } catch (Exception e) {
            LOGGER.error("Failed to copy file with error: {}", e.getMessage(), e);
            throw new RemoteCommandFailureException(remoteCommand, e);
        } finally {
            closeLocalInput();

            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
//
//            if (session != null && session.isConnected()) {
//                session.disconnect();
//            }

            if (channel != null) {
                channelPool.returnObject(channelSessionKey, channel);
            }

        }
    }

    /**
     * Send info about the file
     * @param filePath path and filename
     * @throws IOException
     */
    private void sendFileInfo(final String filePath) throws IOException {
        LOGGER.debug(">>>>>> Sending file INFO of {}", filePath);
        String command;
        File file = new File(filePath);

        // send "C0644 filesize filename", where filename should not include '/'
        long fileSize = file.length();
        command = "C0644 " + fileSize + " ";
        if (filePath.lastIndexOf('/') > 0) {
            command += filePath.substring(filePath.lastIndexOf('/') + 1);
        } else {
            command += filePath;
        }
        command += "\n";
        localInput.write(command.getBytes());
        localInput.flush();

        LOGGER.debug(">>>>>> Finished sending file INFO of {}", filePath);

        if (checkAck(remoteOutput) != 0) {
            throw new RemoteCommandFailureException(remoteCommand, new Throwable("Failed to initialize secure copy"));
        }
    }

    /**
     * Close localInput class variable
     */
    private void closeLocalInput() {
        if (localInput != null) {
            try {
                localInput.close();
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Send the file contents
     * @param filePath the path and filename
     * @throws IOException
     */
    private void sendFileContent(final String filePath) throws IOException {
        byte[] buf = new byte[1024];

        LOGGER.debug(">>>>>> Sending file CONTENTS of {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath)) {
            while (fis.available() > 0) {
                final int size = fis.read(buf);
                localInput.write(buf, 0, size);
            }
        }

        localInput.write(0);
        localInput.flush();

        LOGGER.debug(">>>>>> Done sending file CONTENTS of {}", filePath);

        if (checkAck(remoteOutput) != 0) {
            throw new RemoteCommandFailureException(remoteCommand, new Throwable("Failed to finalize secure copy"));
        } else {
            // Jsch only sets the channel exit status for certain types of channels - scp is NOT one of them
            // checkAck performs a check of the exit status manually so if the remoteOutput is 0 then scp succeeded even though channel.getExitStatus() still returns -1
            checkAckOk = true;
        }
    }

    @Override
    public String getCommandOutputStr() {
        return null;
    }

    @Override
    public String getErrorOutputStr() {
        return null;
    }

    /**
     * Check JSCH acknowledgement response
     * @param in the remote output input stream
     * @return 0 = success, 1 = error, 2 fatal error
     * @throws IOException
     */
    private static int checkAck(final InputStream in) throws IOException {
        final int ack = readAck(in, TIMEOUT);
        if (ack == 0 || ack == -1) {
            return ack;
        } else if (ack == 1 || ack == 2) {
            final String msg = readRemoteOutput(new BufferedInputStream(in), TIMEOUT);
            LOGGER.debug(">>>>>> JSCH SCP checkAck Remote Output: {}", msg);
            if (ack == 1) {
                throw new IOException("ERROR in SCP: " + msg);
            }
            throw new IOException("FATAL ERROR in SCP: " + msg);
        }
        return ack;
    }

    /**
     * Reads the remote output, assumes that input stream will have '\n' to indicate end of stream. If it does not
     * then read input stream will terminate on timeout.
     * @param in a buffered inputstream
     * @param timeout timeout in ms
     * @return output string from remote JSCH
     * @throws IOException
     */
    private static String readRemoteOutput(final BufferedInputStream in, final int timeout) throws IOException {
        final byte [] bytesRead = new byte [1024];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final long startTime = System.currentTimeMillis();
        while (true) {
            if (in.available() > 0) {
                final int size = in.read(bytesRead);
                if (size > 0) {
                    out.write(bytesRead, 0, size);
                }

                if (bytesRead[size - 1] == '\n') {
                    break;
                }
            }

            if ((System.currentTimeMillis() - startTime) > timeout) {
                LOGGER.error("Timeout reading remote output!!!");
                break;
            }
        }

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Read JSCH connection ack
     * @param in the inputstream
     * @param timeout the timeout
     * @return the ack
     * @throws IOException
     */
    private static int readAck(final InputStream in, final int timeout) throws IOException {
        int ack;

        LOGGER.debug(">>>>>> Reading ack...");

        final int readInputSleepDuration = Integer.parseInt(ApplicationProperties.get(JSCH_READ_ACK_SLEEP_DURATION, READ_ACK_SLEEP_DEFAULT_VALUE));
        final long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeout) {
            if (in.available() > 0) {
                ack = in.read();
                LOGGER.debug(">>>>>> Ack = {}", ack);
                return ack;
            }

            try {
                Thread.sleep(readInputSleepDuration);
            } catch (final InterruptedException e) {
                final String errMsg = "readAck was interrupted while waiting for input from JSch channel!";
                LOGGER.error(errMsg, e);
                throw new JscScpCommandProcessorImplException(errMsg, e);
            }
        }
        final String errMsg = "Reading ack timeout!!!";
        LOGGER.error(errMsg);
        throw new JscScpCommandProcessorImplException("Timeout!");
    }

    @Override
    public ExecReturnCode getExecutionReturnCode() {
        // if checkAc k returned 0 then return success (see comment where checkAckOk set to true)
        int returnCode = checkAckOk ? 0 : 1;
        return new ExecReturnCode(returnCode);
    }

    @Override
    public void close() throws IOException {

    }

    /**
     * Prepare the session by setting session properties.
     *
     * @param remoteSystemConnection see {@link RemoteSystemConnection}
     * @return {@link Session}
     * @throws JSchException
     */
    private Session prepareSession(final RemoteSystemConnection remoteSystemConnection) throws JSchException {
        final Session session = jsch.getSession(remoteSystemConnection.getUser(), remoteSystemConnection.getHost(),
                remoteSystemConnection.getPort());
        final char[] encryptedPassword = remoteSystemConnection.getEncryptedPassword();
        if (encryptedPassword != null) {
            session.setPassword(new DecryptPassword().decrypt(encryptedPassword));
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password,gssapi-with-mic,publickey,keyboard-interactive");
        }
        return session;
    }

    /**
     * Get a {@link ChannelExec}
     *
     * @param channelSessionKey the session key that identifies the channel
     * @return {@link ChannelExec}
     * @throws Exception thrown by borrowObject and invalidateObject
     */
    private ChannelExec getChannelExec(final ChannelSessionKey channelSessionKey) throws Exception {
        final long startTime = System.currentTimeMillis();
        Channel channel;
        do {
            LOGGER.debug("borrowing a channel...");
            channel = channelPool.borrowObject(channelSessionKey);
//            if (channel != null) {
//                LOGGER.debug("channel {} borrowed", channel.getId());
//                if (!channel.isConnected()) {
//                    try {
//                        LOGGER.debug("channel {} connecting...", channel.getId());
//                        channel.connect(CHANNEL_CONNECT_TIMEOUT);
//                        LOGGER.debug("channel {} connected!", channel.getId());
//                    } catch (final JSchException jsche) {
//                        LOGGER.error("Borrowed channel {} connection failed! Invalidating the channel...",
//                                channel.getId(), jsche);
//                        channelPool.invalidateObject(channelSessionKey, channel);
//                    }
//                } else {
//                    LOGGER.debug("Channel {} already connected!", channel.getId());
//                }
//            }
//
            if (channel == null && (System.currentTimeMillis() - startTime) > CHANNEL_BORROW_LOOP_WAIT_TIME) {
                final String errMsg = MessageFormat.format("Failed to get a channel within {0} ms! Aborting channel acquisition!",
                        CHANNEL_BORROW_LOOP_WAIT_TIME);
                LOGGER.error(errMsg);
                throw new RuntimeException(errMsg);
            }
        } while (channel == null);
        return (ChannelExec) channel;
    }

}
