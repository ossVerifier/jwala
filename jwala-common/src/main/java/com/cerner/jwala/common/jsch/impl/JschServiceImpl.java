package com.cerner.jwala.common.jsch.impl;

import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.JschServiceException;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.exception.ExitCodeNotAvailableException;
import com.jcraft.jsch.*;
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
 * Implements {@link JschService}
 *
 * Created by JC043760 on 12/26/2016
 */
@Service
public class JschServiceImpl implements JschService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschServiceImpl.class);
    private static final String CRLF = "\r\n";
    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    private static final String EXIT_CODE_START_MARKER = "EXIT_CODE";
    private static final String EXIT_CODE_END_MARKER = "***";

    @Autowired
    private JSch jsch;

    /**
     * Prepare the session by setting session properties
     *
     * @param remoteSystemConnection {@link RemoteSystemConnection}
     * @return {@link Session}
     * @throws JSchException
     */
    @Override
    public Session prepareSession(final RemoteSystemConnection remoteSystemConnection) throws JSchException {
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

    @Override
    public RemoteCommandReturnInfo runCommand(final String command, final Channel channel, long timeout) throws IOException, JSchException {
        if (channel instanceof ChannelShell) {
            return runShellCommand(command, (ChannelShell) channel, timeout);
        }
        return runExecCommand(command, (ChannelExec) channel, timeout);
    }

    /**
     * Runs a command in a shell
     * @param command the command to run
     * @param channelShell the channel where the command is sent for execution
     * @param timeout the length of time in ms in which the method waits for a available byte(s) as a result of command
     * @return result of the command
     * @throws IOException
     */
    private RemoteCommandReturnInfo runShellCommand(final String command, final ChannelShell channelShell, final long timeout)
            throws IOException {
        final InputStream in = channelShell.getInputStream();
        final OutputStream out = channelShell.getOutputStream();

        LOGGER.debug("Executing command \"{}\"...", command);
        out.write(command.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        out.write("echo 'EXIT_CODE='$?***".getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        out.write("echo -n -e '\\xff'".getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        out.flush();

        LOGGER.debug("Reading remote output ...");
        final String remoteOutput = readRemoteOutput(in, (char) 0xff, timeout);
        LOGGER.debug("****** output: start ******");
        LOGGER.debug(remoteOutput);
        LOGGER.debug("****** output: end ******");

        return new RemoteCommandReturnInfo(parseReturnCode(remoteOutput, command), remoteOutput, null);
    }

    /**
     * Runs a command via jsch's exec channel.
     * Unlike the shell channel, an exec channel closes after an execution of a command.
     * @param command the command to run
     * @param channelExec the channel where the command is sent for execution
     * @param timeout the length of time in ms in which the method waits for a available byte(s) as a result of command
     * @return result of the command
     */
    private RemoteCommandReturnInfo runExecCommand(final String command, final ChannelExec channelExec, final long timeout) throws IOException, JSchException {
        LOGGER.debug("Executing command \"{}\"...", command);
        channelExec.setCommand(command.getBytes(StandardCharsets.UTF_8));

        final InputStream remoteOutput = channelExec.getInputStream();
        final InputStream remoteError = channelExec.getErrStream();

        LOGGER.debug("channel {} connecting...", channelExec.getId());
        channelExec.connect(CHANNEL_CONNECT_TIMEOUT);
        LOGGER.debug("channel {} connected!", channelExec.getId());

        LOGGER.debug("Channel exec exit status = {}", channelExec.getExitStatus());

        final String output = readRemoteOutput(remoteOutput, null, timeout);
        LOGGER.debug("remote output = {}", output);

        String errorOutput = null;
        if (channelExec.getExitStatus() != 0) {
            errorOutput = readRemoteOutput(remoteError, null, timeout);
            LOGGER.debug("remote error output = {}", errorOutput);
        }

        return new RemoteCommandReturnInfo(channelExec.getExitStatus(), output, errorOutput);
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
    private String readRemoteOutput(final InputStream remoteOutput, final Character dataEndMarker, final long timeout)
            throws IOException {
        final StringBuilder remoteOutputStringBuilder = new StringBuilder();
        long startTime = System.currentTimeMillis();
        while (true) {
            if (remoteOutput.available() != 0) {
                char readChar = Character.toChars(remoteOutput.read())[0];
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
     * @param command the command string
     * @return {@link ExecReturnCode}
     */
    private int parseReturnCode(final String outputStr, final String command) {
        if (outputStr != null) {
            try {
                final String exitCodeStr = outputStr.substring(outputStr.lastIndexOf(EXIT_CODE_START_MARKER)
                        + EXIT_CODE_START_MARKER.length() + 1, outputStr.lastIndexOf(EXIT_CODE_END_MARKER));
                return Integer.parseInt(exitCodeStr);
            } catch (final IndexOutOfBoundsException e) {
                final String errorMsg = MessageFormat.format("Failed to parse output: {0}", outputStr);
                LOGGER.error(errorMsg, e);
                throw new JschServiceException(errorMsg, e);
            }
        }
        throw new ExitCodeNotAvailableException(command);
    }

}
