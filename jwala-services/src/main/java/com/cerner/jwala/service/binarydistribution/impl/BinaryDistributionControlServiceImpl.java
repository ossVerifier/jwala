package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.control.binarydistribution.command.impl.WindowsBinaryDistributionPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by SP043299 on 9/7/2016.
 */
public class BinaryDistributionControlServiceImpl implements BinaryDistributionControlService {
    private final RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor;
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionControlServiceImpl.class);

    public BinaryDistributionControlServiceImpl(RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor) {
        this.remoteCommandExecutor = remoteCommandExecutor;
    }

    @Override
    public CommandOutput secureCopyFile(final String hostname, final String source, final String destination) {
        CommandOutput commandOutput;
        try {
            commandOutput = remoteCommandExecutor.executeRemoteCommand(null,
                    hostname,
                    BinaryDistributionControlOperation.SECURE_COPY,
                    new WindowsBinaryDistributionPlatformCommandProvider(),
                    source,
                    destination);
        } catch (CommandFailureException e) {
            LOGGER.error(e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error secureCopy File from host: " + hostname + " source: "
                    + source + " destination: " + destination, e);
        }
        return commandOutput;
    }

    @Override
    public CommandOutput createDirectory(final String hostname, final String destination) {
        CommandOutput commandOutput;
        try {
            commandOutput = remoteCommandExecutor.executeRemoteCommand(null,
                    hostname,
                    BinaryDistributionControlOperation.CREATE_DIRECTORY,
                    new WindowsBinaryDistributionPlatformCommandProvider(),
                    destination);
        } catch (CommandFailureException e) {
            LOGGER.error(e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error create directory at host: " + hostname + " destination: " + destination, e);
        }
        return commandOutput;
    }

    @Override
    public CommandOutput checkFileExists(final String hostname, final String destination) {
        CommandOutput commandOutput;
        try {
            commandOutput = remoteCommandExecutor.executeRemoteCommand(null,
                    hostname,
                    BinaryDistributionControlOperation.CHECK_FILE_EXISTS,
                    new WindowsBinaryDistributionPlatformCommandProvider(),
                    destination);

        } catch (CommandFailureException e) {
            LOGGER.error(e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error check File Exists at host: " + hostname + " destination: " + destination, e);
        }
        return commandOutput;
    }

    @Override
    public CommandOutput unzipBinary(final String hostname, final String binaryLocation) {
        CommandOutput commandOutput;
        try {
            commandOutput = remoteCommandExecutor.executeRemoteCommand(null,
                    hostname,
                    BinaryDistributionControlOperation.UNZIP_BINARY,
                    new WindowsBinaryDistributionPlatformCommandProvider(),
                    binaryLocation);
        } catch (CommandFailureException e) {
            LOGGER.error(e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error unzip Binary at host: " + hostname + " binaryLocation: " + binaryLocation, e);
        }
        return commandOutput;
    }

    @Override
    public CommandOutput deleteBinary(final String hostname, final String destination)  {
        CommandOutput commandOutput;
        try{
            commandOutput = remoteCommandExecutor.executeRemoteCommand(null,
                    hostname,
                    BinaryDistributionControlOperation.DELETE_BINARY,
                    new WindowsBinaryDistributionPlatformCommandProvider(),
                    destination);
        } catch (CommandFailureException e){
            LOGGER.error(e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error delete Binary at host: " + hostname + " destination: " + destination, e);
        }
        return commandOutput;
    }
}
