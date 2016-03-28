package com.siemens.cto.aem.service.impl.spring.component;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelType;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.exception.ExitCodeNotAvailableException;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
import com.siemens.cto.aem.service.exception.RemoteCommandExecutorServiceException;
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
 *
 * Created by JC043760 on 3/25/2016.
 */
@Service
public class JschRemoteCommandExecutorServiceImpl implements RemoteCommandExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschRemoteCommandExecutorServiceImpl.class);
    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    private static final int CHANNEL_BORROW_LOOP_WAIT_TIME = 180000;
    private static final int REMOTE_OUTPUT_STREAM_MAX_WAIT_TIME = 180000;
    private static final String EXIT_CODE_START_MARKER = "EXIT_CODE";
    private static final String EXIT_CODE_END_MARKER = "***";

    private final JSch jsch = new JSch();
    private final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Autowired
    public JschRemoteCommandExecutorServiceImpl(final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool) {
        this.channelPool = channelPool;
    }

    @Override
    public RemoteCommandReturnInfo executeCommand(RemoteExecCommand remoteExecCommand) {
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

            String commandOutputStr = readRemoteOutput(in);
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
    protected ExecReturnCode parseReturnCode(final String outputStr, final RemoteExecCommand remoteExecCommand) {
        if (outputStr != null) {
            final String exitCodeStr = outputStr.substring(outputStr.lastIndexOf(EXIT_CODE_START_MARKER)
                    + EXIT_CODE_START_MARKER.length() + 1, outputStr.lastIndexOf(EXIT_CODE_END_MARKER));
            return new ExecReturnCode(Integer.parseInt(exitCodeStr));
        }
        throw new ExitCodeNotAvailableException(remoteExecCommand.getCommand().toCommandString());
    }

}
