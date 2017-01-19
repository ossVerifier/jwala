package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.resource.EntityType;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.binarydistribution.DistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public class BinaryDistributionServiceImpl implements BinaryDistributionService, DistributionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionServiceImpl.class);
    @Autowired
    protected SshConfiguration sshConfig;

    private static final String UNZIPEXE = "unzip.exe";
    private static final String APACHE_EXCLUDE = "ReadMe.txt *--";

    private final BinaryDistributionControlService binaryDistributionControlService;
    private final BinaryDistributionLockManager binaryDistributionLockManager;

    public BinaryDistributionServiceImpl(BinaryDistributionControlService binaryDistributionControlService, BinaryDistributionLockManager binaryDistributionLockManager) {
        this.binaryDistributionControlService = binaryDistributionControlService;
        this.binaryDistributionLockManager = binaryDistributionLockManager;
    }

    @Override
    public void distributeWebServer(final String hostname) {
        String writeLockResourceName = hostname;
        try {
            binaryDistributionLockManager.writeLock(writeLockResourceName);
            String apacheDirName = ApplicationProperties.get(PropertyKeys.REMOTE_PATHS_HTTPD_ROOT_DIR_NAME);
            String remoteDeployDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_DEPLOY_DIR);
            String httpdZipFile = ApplicationProperties.getRequired(PropertyKeys.APACHE_HTTPD_FILE_NAME);
            distributeBinary(hostname, apacheDirName, httpdZipFile, remoteDeployDir, APACHE_EXCLUDE);
        } finally {
            binaryDistributionLockManager.writeUnlock(writeLockResourceName);
        }
    }

    @Override
    public void distributeJdk(final String hostname) {
        LOGGER.info("Start deploy jdk for host {}", hostname);
        String remoteDeployDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_DEPLOY_DIR);
        String javaDirName = ApplicationProperties.getRequired(PropertyKeys.REMOTE_JWALA_JAVA_ROOT_DIR_NAME);
        String jdkBinary = ApplicationProperties.get(PropertyKeys.JDK_BINARY_FILE_NAME);
        distributeBinary(hostname, javaDirName,jdkBinary, remoteDeployDir, "");
        LOGGER.info("End deploy jdk for {}", hostname);
    }

    private void distributeBinary(final String hostname, final String zipFileRootDir, final String zipFileName, final String jwalaRemoteHome, final String exclude) {
        String jwalaBinaryDir = ApplicationProperties.getRequired(PropertyKeys.LOCAL_JWALA_BINARY_DIR);
        String binaryDeployDir = jwalaRemoteHome +"/"+zipFileRootDir;
        String zipFile = jwalaBinaryDir +"/"+zipFileName;
        if (remoteFileCheck(hostname, binaryDeployDir)) {
            LOGGER.info("Found {} on host {}. Nothing to do.", binaryDeployDir, hostname);
            return;
        }
        LOGGER.info("Binary {} on host {} not found. Trying to deploy it", binaryDeployDir, hostname);
        String destinationZipFile = binaryDeployDir + "/" + zipFile;
        remoteCreateDirectory(hostname, jwalaRemoteHome);
        remoteSecureCopyFile(hostname, zipFile, jwalaRemoteHome+"/"+zipFileName);
        remoteUnzipBinary(hostname, jwalaRemoteHome +"/"+zipFileName, jwalaRemoteHome+"/", exclude);
        remoteDeleteBinary(hostname, jwalaRemoteHome+"/"+zipFileName);
    }

    @Override
    public void changeFileMode(final String hostname, final String mode, final String targetDir, final String target) {
        try {
            if (binaryDistributionControlService.changeFileMode(hostname, mode, targetDir, target).getReturnCode().wasSuccessful()) {
                LOGGER.info("change file mode " + mode + " at targetDir " + targetDir);
            } else {
                String message = "Failed to change the file permissions in " + targetDir + "/" + target;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in change file mode at host: " + hostname + " mode: " + mode + " target: " + target;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteDeleteBinary(final String hostname, final String destination) {
        try {
            if (binaryDistributionControlService.deleteBinary(hostname, destination).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully delete the binary {}", destination);
            } else {
                final String message = "error in deleting file " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {

            final String message = "Error in delete remote binary at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteUnzipBinary(final String hostname, final String zipFileName, final String destination, final String exclude) {
        try {
            if (binaryDistributionControlService.unzipBinary(hostname, zipFileName, destination, exclude).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully unzipped the binary {}", zipFileName);
            } else {
                final String message = "cannot unzip from " + zipFileName+ " to " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = String.format("Error in remote unzip binary at host: %s binaryLocation: %s destination: %s", hostname, zipFileName, destination);
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteSecureCopyFile(final String hostname, final String source, final String destination) {
        try {
            if (binaryDistributionControlService.secureCopyFile(hostname, source, destination).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully copied the binary {} over to {}", source, destination);
            } else {
                final String message = "error with scp of binary " + source + " to destination " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error issuing SCP to host " + hostname + " using source " + source +
                    " and destination " + destination + ". Exception is " + e.getMessage();
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteCreateDirectory(final String hostname, final String remoteDir) {
        LOGGER.debug("Attempting to create directory {} on host {}", remoteDir, hostname);
        try {
            if (binaryDistributionControlService.createDirectory(hostname, remoteDir).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully created directories {}", remoteDir);
            } else {
                final String message = "User does not have permission to create the directory " + remoteDir;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in create remote directory at host: " + hostname + " destination: " + remoteDir;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }
    @Override
    public boolean remoteFileCheck(final String hostname, final String remoteFilePath) {
        LOGGER.info("Looking for the remote file {} on host {}", remoteFilePath, hostname);
        boolean result;
        try {
            result = binaryDistributionControlService.checkFileExists(hostname, remoteFilePath).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in check remote File at host: " + hostname + " destination: " + remoteFilePath;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        LOGGER.info("Remote file {} {}", remoteFilePath, result ? "found" : "not found");
        return result;
    }

    @Override
    public void distributeUnzip(String hostname) {
        LOGGER.info("Start deploy unzip for {}", hostname);
        final String jwalaScriptsPath = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
        if (!remoteFileCheck(hostname, jwalaScriptsPath)) {
            remoteCreateDirectory(hostname, jwalaScriptsPath);
        }

        if (!remoteFileCheck(hostname, jwalaScriptsPath + "/" + UNZIPEXE)) {
            final String unzipFile= ApplicationProperties.get(PropertyKeys.LOCAL_JWALA_BINARY_DIR) + "/" + UNZIPEXE;
            LOGGER.info("SCP {} " + unzipFile);
            remoteSecureCopyFile(hostname, unzipFile, jwalaScriptsPath);
            changeFileMode(hostname, "a+x", jwalaScriptsPath, UNZIPEXE);
        }

        LOGGER.info("End deploy unzip for {}", hostname);
    }

    @Override
    public void backupFile(final String hostname, final String remoteFilePath){
        binaryDistributionControlService.backupFile(hostname,remoteFilePath);
    }
}