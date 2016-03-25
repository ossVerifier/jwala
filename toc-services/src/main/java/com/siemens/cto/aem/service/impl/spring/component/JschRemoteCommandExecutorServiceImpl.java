package com.siemens.cto.aem.service.impl.spring.component;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelType;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of {@link RemoteCommandExecutorService} using JSCH.
 *
 * Created by JC043760 on 3/25/2016.
 */
@Service
public class JschRemoteCommandExecutorServiceImpl implements RemoteCommandExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschRemoteCommandExecutorServiceImpl.class);
    private final JSch jsch = new JSch();

    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;

    @Override
    public RemoteCommandReturnInfo executeCommand(RemoteExecCommand remoteExecCommand) {
        Session session = null;
        ChannelExec channel = null;
        RemoteCommandReturnInfo remoteCommandReturnInfo;
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

            remoteCommandReturnInfo = new RemoteCommandReturnInfo(channel.getExitStatus(), remoteOutputStringBuilder.toString(),
                    remoteErrorStringBuilder.toString());
            LOGGER.debug("RemoteCommandReturnInfo = {}", remoteCommandReturnInfo);

        } catch (final JSchException | IOException e) {
            LOGGER.error("Error processing exec command!", e);
            remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, StringUtils.EMPTY, e.getMessage());
            LOGGER.debug("RemoteCommandReturnInfo = {}", remoteCommandReturnInfo);
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
        return remoteCommandReturnInfo;
    }

    /**
     * Prepare the session by setting session properties.
     * @param remoteSystemConnection {@link RemoteSystemConnection}
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
