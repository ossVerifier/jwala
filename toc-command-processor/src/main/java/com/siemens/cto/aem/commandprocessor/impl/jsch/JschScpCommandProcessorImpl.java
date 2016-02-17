package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JschScpCommandProcessorImpl extends JschCommandProcessorImpl {
    private boolean checkAckOk;
    private static final Logger LOGGER = LoggerFactory.getLogger(JschScpCommandProcessorImpl.class);

    public JschScpCommandProcessorImpl(JSch jsch, RemoteExecCommand remoteCommand) {
        super(jsch, remoteCommand);
    }

    /**
     * Taken from the Jsch example ScpTo.java
     * @throws RemoteCommandFailureException
     */
    @Override
    public void processCommand() throws RemoteCommandFailureException {
        checkAckOk = false;
        FileInputStream fis = null;
        List<String> commandFragments = theCommand.getCommand().getCommandFragments();
        try {
            final RemoteSystemConnection remoteSystemConnection = theCommand.getRemoteSystemConnection();
            session = prepareSession(theJsch, remoteSystemConnection);
            session.connect();

            String target = commandFragments.get(2);

            // exec 'scp -t rfile' remotely
            String command = "scp -t " + target;
            channel = session.openChannel("exec");
            final ChannelExec channelExec = (ChannelExec) channel;
            channelExec.setCommand(command);

            // get I/O streams for remote scp
            localInput = channelExec.getOutputStream();
            remoteOutput = channelExec.getInputStream();
            remoteError = channelExec.getErrStream();

            channelExec.connect();

            if (checkAck(remoteOutput) != 0) {
                throw new RemoteCommandFailureException(theCommand, new Throwable("Failed to connect to the remote host during secure copy"));
            }

            final String lfilePath = commandFragments.get(1);
            File lfile = new File(lfilePath);

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = lfile.length();
            command = "C0644 " + filesize + " ";
            if (lfilePath.lastIndexOf('/') > 0) {
                command += lfilePath.substring(lfilePath.lastIndexOf('/') + 1);
            } else {
                command += lfilePath;
            }
            command += "\n";
            localInput.write(command.getBytes());
            localInput.flush();
            if (checkAck(remoteOutput) != 0) {
                throw new RemoteCommandFailureException(theCommand, new Throwable("Failed to initialize secure copy"));
            }

            // send content of lfile
            fis = new FileInputStream(lfilePath);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                localInput.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            localInput.write(buf, 0, 1);
            localInput.flush();
            if (checkAck(remoteOutput) != 0) {
                throw new RemoteCommandFailureException(theCommand, new Throwable("Failed to finalize secure copy"));
            } else {
                // Jsch only sets the channel exit status for certain types of channels - scp is NOT one of them
                // checkAck performs a check of the exit status manually so if the remoteOutput is 0 then scp succeeded even though channel.getExitStatus() still returns -1
                checkAckOk = true;
            }
        } catch (Exception e) {
            throw new RemoteCommandFailureException(theCommand, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            if (localInput != null) {
                try {
                    localInput.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            if (channel.isConnected()) {
                channel.disconnect();
            }
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer(); // TODO: Find out if we can use StringBuilder instead.
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }  while (c != '\n');
            if (b == 1) { // error
                throw new IOException("ERROR in secure copy: " + sb.toString());
            }
            throw new IOException("FATAL ERROR in secure copy: " + sb.toString());
        }
        return b;
    }

    @Override
    public ExecReturnCode getExecutionReturnCode() {
        // if checkAck returned 0 then return success (see comment where checkAckOk set to true)
        int returnCode = checkAckOk ? 0 : 1;
        return new ExecReturnCode(returnCode);
    }

}
