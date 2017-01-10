package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.commandprocessor.impl.jsch.JschScpCommandProcessorImpl;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.configuration.AemSshConfig;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.jcraft.jsch.JSchException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public class BinaryDistributionControlServiceImpl implements BinaryDistributionControlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionControlServiceImpl.class);
    private final RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor;
    @Autowired
    protected SshConfiguration sshConfig;

    @Autowired
    private AemSshConfig aemSshConfig;
    @Autowired
    protected RemoteCommandExecutorService remoteCommandExecutorService;

    static String CREATE_DIR="if [ ! -e \"%s\" ]; then mkdir -p \"%s\"; fi;";
    static String REMOVE="rm";
    static String SECURE_COPY = "scp";
    static String TEST = "test -e";
    static String CHMOD = "chmod";
    public BinaryDistributionControlServiceImpl(RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor) {
        this.remoteCommandExecutor = remoteCommandExecutor;
    }

    @Override
    public CommandOutput secureCopyFile(final String hostname, final String source, final String destination) throws CommandFailureException  {
//TODO: refactor scp
        RemoteExecCommand command = new RemoteExecCommand(getConnection(hostname),  new ExecCommand(SECURE_COPY, source, destination));
        try {
            final JschScpCommandProcessorImpl jschScpCommandProcessor = new JschScpCommandProcessorImpl(aemSshConfig.getJschBuilder().build(), command);
            jschScpCommandProcessor.processCommand();
            jschScpCommandProcessor.close();
            return  new CommandOutput(new ExecReturnCode(jschScpCommandProcessor.getExecutionReturnCode().getReturnCode()),
                    jschScpCommandProcessor.getCommandOutputStr(), jschScpCommandProcessor.getErrorOutputStr());
        } catch (Throwable th) {
            throw new ApplicationException(th);
        }
    }

    @Override
    public CommandOutput createDirectory(final String hostname, final String destination) throws CommandFailureException {
        ExecCommand command = new ExecCommand(String.format(CREATE_DIR, destination,destination));
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),command  ));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput checkFileExists(final String hostname, final String destination) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(TEST, destination)));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput unzipBinary(final String hostname, final String zipPath, final String destination, final String exclude) throws CommandFailureException {
        String command = getUnzipCommand(zipPath,destination);
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(command)));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput deleteBinary(final String hostname, final String destination) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(REMOVE, destination)));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput changeFileMode(String hostname, String mode, String targetDir, String target) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand(CHMOD, mode, targetDir, target )));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    @Override
    public CommandOutput getUName(String hostname) throws CommandFailureException {
        RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(hostname),  new ExecCommand("uname")));
        CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode),
                remoteCommandReturnInfo.standardOuput, remoteCommandReturnInfo.errorOupout);
        return commandOutput;
    }

    public void printCommandOutput(CommandOutput commandOutput) {
        LOGGER.info(commandOutput.getStandardOutput());
        LOGGER.info(commandOutput.getStandardError());
    }

    /**
     *
     * @param host
     * @return
     */
    private RemoteSystemConnection getConnection(String host) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getPassword(), host, sshConfig.getPort());
    }

    private String getUnzipCommand(String zipFileName, String destination){
            if(zipFileName.indexOf(".zip")>-1){
            return "~/.jwala/unzip.exe + \" -q -o \" + aParams[1] + \" -d \" + aParams[2] + \" -x \" + aParams[3]";
        }else if(zipFileName.indexOf(".gz")>-1){
            return String.format("tar xvf %s -C %s", zipFileName, destination);
        }else{
            throw new ApplicationException("Unknown zip file format");
        }
    }
}

