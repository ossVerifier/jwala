package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.control.command.impl.WindowsBinaryDistributionPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public class BinaryDistributionControlServiceImpl implements BinaryDistributionControlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionControlServiceImpl.class);
    private final RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor;

    public BinaryDistributionControlServiceImpl(RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor) {
        this.remoteCommandExecutor = remoteCommandExecutor;
    }

    @Override
    public CommandOutput secureCopyFile(final String hostname, final String source, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.SECURE_COPY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                source,
                destination);
    }

    @Override
    public CommandOutput createDirectory(final String hostname, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.CREATE_DIRECTORY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                destination);
    }

    @Override
    public CommandOutput checkFileExists(final String hostname, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.CHECK_FILE_EXISTS,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                destination);
    }

    @Override
    public CommandOutput unzipBinary(final String hostname, final String zipPath, final String binaryLocation, final String destination, final String exclude) throws CommandFailureException {
        CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.UNZIP_BINARY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                zipPath,
                binaryLocation,
                destination,
                exclude);
        printCommandOutput(commandOutput);
        return commandOutput;
    }

    @Override
    public CommandOutput deleteBinary(final String hostname, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.DELETE_BINARY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                destination);
    }

    @Override
    public CommandOutput changeFileMode(String hostname, String mode, String targetDir, String target) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.CHANGE_FILE_MODE,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                mode,
                targetDir,
                target);
    }

    @Override
    public CommandOutput getUName(String hostname) throws CommandFailureException {
        final ExecCommand expecCommand = new ShellCommand("uname");
         return remoteCommandExecutor.executeRemoteCommand(null,
                hostname,
                BinaryDistributionControlOperation.UNAME,
                new WindowsBinaryDistributionPlatformCommandProvider()
         );
    }

    public void printCommandOutput(CommandOutput commandOutput) {
        LOGGER.info(commandOutput.getStandardOutput());
        LOGGER.info(commandOutput.getStandardError());
    }
}
