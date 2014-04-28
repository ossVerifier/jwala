package com.siemens.cto.aem.commandprocessor.impl.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;

public class JschCommandProcessor implements CommandProcessor {

    private final Session session;
    private final Channel channel;
    private final InputStream remoteOutput;
    private final InputStream remoteError;
    private final OutputStream localInput;

    public JschCommandProcessor(final JSch theJsch,
                                final String theCommand,
                                final String theUser,
                                final String theHost,
                                final Integer thePort) throws RemoteCommandFailureException {

        try {
            session = theJsch.getSession(theUser,
                                         theHost,
                                         thePort);
            session.connect();
            channel = session.openChannel("exec");
            final ChannelExec channelExec = (ChannelExec)channel;
            channelExec.setCommand(theCommand.getBytes(StandardCharsets.UTF_8));
            remoteOutput = channelExec.getInputStream();
            remoteError = channelExec.getErrStream();
            localInput = channelExec.getOutputStream();
            channelExec.connect();

        } catch (final JSchException | IOException e) {
            throw new RemoteCommandFailureException(theCommand,
                                                    theUser,
                                                    theHost,
                                                    thePort.toString(),
                                                    e);
        }
    }

    @Override
    public InputStream getCommandOutput() {
        return remoteOutput;
    }

    @Override
    public InputStream getErrorOutput() {
        return remoteError;
    }

    @Override
    public OutputStream getCommandInput() {
        return localInput;
    }

    @Override
    public void close() throws Exception {
        channel.disconnect();
        session.disconnect();
    }
}
