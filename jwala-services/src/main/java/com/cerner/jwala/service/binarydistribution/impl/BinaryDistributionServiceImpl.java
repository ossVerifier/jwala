package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.resource.EntityType;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.text.MessageFormat;

/**
 * Created by Arvindo Kinny on 10/11/2016
 */
public class BinaryDistributionServiceImpl implements BinaryDistributionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionServiceImpl.class);

    private static final String BINARY_LOCATION_PROPERTY_KEY = "jwala.binary.dir";
    private static final String UNZIPEXE = "unzip.exe";
    private static final String APACHE_EXCLUDE = "ReadMe.txt *--";

    private final BinaryDistributionControlService binaryDistributionControlService;
    private final BinaryDistributionLockManager binaryDistributionLockManager;
    private HistoryFacadeService historyFacadeService;

    public BinaryDistributionServiceImpl(BinaryDistributionControlService binaryDistributionControlService, BinaryDistributionLockManager binaryDistributionLockManager, HistoryFacadeService historyFacadeService) {
        this.binaryDistributionControlService = binaryDistributionControlService;
        this.binaryDistributionLockManager = binaryDistributionLockManager;
        this.historyFacadeService = historyFacadeService;
    }

    @Override
    public void distributeWebServer(final String hostname) {
        String writeLockResourceName = hostname + "-" + EntityType.WEB_SERVER.toString();
        try {
            binaryDistributionLockManager.writeLock(writeLockResourceName);
            File apache = new File(ApplicationProperties.get("remote.paths.apache.httpd"));
            String webServerDir = apache.getName();
            String binaryDeployDir = apache.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
            final String localArchivePath = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY) + "/" + webServerDir + ".zip";
            distributeBinary(hostname, webServerDir, binaryDeployDir, APACHE_EXCLUDE, localArchivePath);
        } finally {
            binaryDistributionLockManager.writeUnlock(writeLockResourceName);
        }
    }

    @Override
    public void distributeJdk(final Jvm jvm) {
        LOGGER.info("Start deploy jdk for {}", jvm);
        final Media jdkMedia = jvm.getJdkMedia();
        final String binaryDeployDir = new File(jdkMedia.getRemoteHostPath()).getAbsolutePath().replaceAll("\\\\", "/");
        if (binaryDeployDir != null && !binaryDeployDir.isEmpty()) {
            historyFacadeService.write(jvm.getHostName(), jvm.getGroups(), "DISTRIBUTE_JDK " + jdkMedia.getName(),
                    EventType.APPLICATION_EVENT, getUserNameFromSecurityContext());
            if (!checkIfMediaDirExists(jvm.getJdkMedia().getMediaDir().split(","), jvm.getHostName(), binaryDeployDir)) {
                distributeBinary(jvm.getHostName(), jdkMedia.getName(), binaryDeployDir, "", jdkMedia.getPath());
            } else {
                LOGGER.warn("Jdk directories already exists, installation of {} skipped!", jvm.getJdkMedia().getName());
            }
        } else {
            final String errMsg = MessageFormat.format("JDK dir location is null or empty for JVM {0}. Not deploying JDK.", jvm.getJvmName());
            LOGGER.error(errMsg);
            throw new ApplicationException(errMsg);
        }
        LOGGER.info("End deploy jdk {} for {}", jdkMedia.getName(), jvm.getJvmName());
    }

    private void distributeBinary(final String hostname, final String binaryName, final String binaryDeployDir, final String excludeFromZip, String localArchivePath) {
        if (binaryDeployDir == null || binaryDeployDir.isEmpty()) {
            LOGGER.warn("Binary deploy location not provided value is {}", binaryDeployDir);
            return;
        }

        if (remoteFileCheck(hostname, binaryDeployDir + "/" + binaryName)) {
            LOGGER.warn("Found {} on host {}. Nothing to do.", binaryName, hostname);
            return;
        }

        if (localArchivePath == null || localArchivePath.isEmpty()) {
            LOGGER.warn("Cannot find the binary directory location in jwala, value is {}", localArchivePath);
            return;
        }

        LOGGER.info("Binary {} on host {} not found. Trying to deploy it", binaryName, hostname);

        String zipFile = localArchivePath;
        String destinationZipFile = binaryDeployDir + "/" + binaryName + ".zip";
        remoteCreateDirectory(hostname, binaryDeployDir);
        remoteSecureCopyFile(hostname, zipFile, destinationZipFile);

        try {
            remoteUnzipBinary(hostname,
                    ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" + UNZIPEXE,
                    destinationZipFile,
                    binaryDeployDir,
                    excludeFromZip);
        } finally {
            remoteDeleteBinary(hostname, destinationZipFile);
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

    public void remoteUnzipBinary(final String hostname, final String zipPath, final String binaryLocation, final String destination, final String exclude) {
        try {
            if (binaryDistributionControlService.unzipBinary(hostname, zipPath, binaryLocation, destination, exclude).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully unzipped the binary {}", binaryLocation);
            } else {
                final String message = "cannot unzip from " + binaryLocation + " to " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in remote unzip binary at host: " + hostname + " binaryLocation: " + binaryLocation + " destination: " + destination;
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
     * Checks if the binary media directories already exists
     * @param mediaDirs the binary media directories to check
     * @param hostName the host name where to check the binary media directories
     * @param binaryDeployDir the location where the binary media directories are in
     * @return true if all the binary media root directories already exists, otherwise false
     */
    private boolean checkIfMediaDirExists(final String [] mediaDirs, final String hostName, final String binaryDeployDir) {
        for (final String mediaDir : mediaDirs) {
            if (!remoteFileCheck(hostName, binaryDeployDir + "/" + mediaDir)) {
                return false;
            }
        }
        return true;
    }

    private String getUserNameFromSecurityContext() {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            LOGGER.error("No context found getting user name from SecurityContextHolder");
            throw new SecurityException("No context found getting user name from SecurityContextHolder");
        }

        final Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            LOGGER.debug("No authentication found getting user name from SecuriyContextHolder");
            return "";
        }

        return authentication.getName();
    }

}