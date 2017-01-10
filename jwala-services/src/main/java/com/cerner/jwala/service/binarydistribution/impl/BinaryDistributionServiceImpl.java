package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.resource.EntityType;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public class BinaryDistributionServiceImpl implements BinaryDistributionService {
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
        String writeLockResourceName = hostname + "-" + EntityType.WEB_SERVER.toString();
        try {
            binaryDistributionLockManager.writeLock(writeLockResourceName);
            String remoteDeployDir =  ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_APACHE_HTTPD);
            String apacheDirName = ApplicationProperties.get(PropertyKeys.REMOTE_PATHS_HTTPD_ROOT_DIR_NAME);
            distributeBinary(hostname, apacheDirName, remoteDeployDir, APACHE_EXCLUDE);
        } finally {
            binaryDistributionLockManager.writeUnlock(writeLockResourceName);
        }
    }

    @Override
    public void distributeJdk(final String hostname) {
        LOGGER.info("Start deploy jdk for host {}", hostname);
        String remoteDeployDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_JAVA_HOME);
        String javaDirName = ApplicationProperties.getRequired(PropertyKeys.REMOTE_JWALA_JAVA_ROOT_DIR_NAME);
        distributeBinary(hostname, javaDirName, remoteDeployDir, "");
        LOGGER.info("End deploy jdk for {}", hostname);
    }

    private void distributeBinary(final String hostname, final String binaryName, final String binaryDeployDir, final String excludeFromZip) {
        String binaryDir = ApplicationProperties.getRequired(PropertyKeys.LOCAL_JWALA_BINARY_DIR);
        LOGGER.debug("SCP binary starting for remote host {}. binary name is {}, binary deploy dir is {}", hostname, binaryName, binaryDeployDir);

        if (remoteFileCheck(hostname, binaryDeployDir)) {
            LOGGER.info("Found {} on host {}. Nothing to do.", binaryName, hostname);
            return;
        }

        LOGGER.info("Binary {} on host {} not found. Trying to deploy it", binaryName, hostname);

        String zipFile = binaryDir + "/" + binaryName + ".zip";
        String destinationZipFile = binaryDeployDir + ".zip";

        remoteCreateDirectory(hostname, binaryDeployDir);
        remoteSecureCopyFile(hostname, zipFile, destinationZipFile);

        try {
            remoteUnzipBinary(hostname,
                    ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" + UNZIPEXE,
                    destinationZipFile,
                    ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_DEPLOY_DIR),
                    excludeFromZip);
        } finally {
            remoteDeleteBinary(hostname, destinationZipFile);
        }

    private void distributeBinary(final String hostname, final String zipFileRootDir, final String zipFileName, final String jwalaRemoteHome, final String exclude) {
//TODO: cleanup if logic
        String jwalaBinaryDir = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY);
        String binaryDeployDir = jwalaRemoteHome +"/"+zipFileRootDir;
        String zipFile = jwalaBinaryDir +"/"+zipFileName;
        if (binaryDeployDir != null && !binaryDeployDir.isEmpty()) {
            if (!remoteFileCheck(hostname, binaryDeployDir)) {
                LOGGER.info("Couldn't find {} on host {}. Trying to deploy it", zipFileRootDir, hostname);
                if (binaryDeployDir != null && !binaryDeployDir.isEmpty()) {
                    String destinationZipFile = binaryDeployDir + "/" + zipFile;
                    remoteCreateDirectory(hostname, jwalaRemoteHome);
                    remoteSecureCopyFile(hostname, zipFile, jwalaRemoteHome+"/"+zipFileName);
                    remoteUnzipBinary(hostname, jwalaRemoteHome +"/"+zipFileName, jwalaRemoteHome+"/", exclude);
                } else {
                    LOGGER.warn("Cannot find the binary directory location in jwala, value is {}", binaryDeployDir);
                }
            } else {
                LOGGER.info("Found {} at on host {}", binaryDeployDir, hostname);
            }
        } else {
            LOGGER.warn("Binary deploy location not provided value is {}", binaryDeployDir);
            throw new ApplicationException("Binary deploy location not provided value is "+ binaryDeployDir);
        }
    }

    public void changeFileMode(final String hostname, final String mode, final String targetDir, final String target) {
        try {
            if (binaryDistributionControlService.changeFileMode(hostname, mode, targetDir, target).getReturnCode().wasSuccessful()) {
                LOGGER.info("change file mode " + mode + " at targetDir " + targetDir);
            } else {
                String message = "Failed to change the file permissions in " + targetDir + "/" + UNZIPEXE;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in change file mode at host: " + hostname + " mode: " + mode + " target: " + target;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

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

    public void remoteUnzipBinary(final String hostname, final String zipFileName, final String destination, final String exclude) {
        try {
//unzip
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

    /**
     *
     * @param host
     * @return
     */
    private RemoteSystemConnection getConnection(String host) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getPassword(), host, sshConfig.getPort());
    }
}