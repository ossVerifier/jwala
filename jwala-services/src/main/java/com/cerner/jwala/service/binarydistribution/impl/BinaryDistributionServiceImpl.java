package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.zip.ZipDirectory;
import com.cerner.jwala.service.zip.impl.ZipDirectoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Created by SP043299 on 9/6/2016.
 */
@Service
public class BinaryDistributionServiceImpl implements BinaryDistributionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionServiceImpl.class);

    private static final String BINARY_LOCATION_PROPERTY_KEY = "jwala.binary.dir";

    private final ZipDirectory zipDirectory = new ZipDirectoryImpl();
    private final BinaryDistributionControlService binaryDistributionControlService;

    @Autowired
    public BinaryDistributionServiceImpl(BinaryDistributionControlService binaryDistributionControlService) {
        this.binaryDistributionControlService = binaryDistributionControlService;
    }

    @Override
    public void distributeJdk(final String hostname) {
        File javaHome = new File(ApplicationProperties.get("stp.java.home"));
        String jdkDir = javaHome.getName();
        String binaryDeployDir = javaHome.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        if (jdkDir != null && !jdkDir.isEmpty()) {
            distributeBinary(hostname, jdkDir, binaryDeployDir);
        } else {
            LOGGER.warn("JDK dir location is null or empty {}", jdkDir);
        }
    }

    @Override
    public void distributeTomcat(final String hostname) {
        File tomcat = new File(ApplicationProperties.get("remote.paths.tomcat.core"));
        String tomcatDir = tomcat.getParentFile().getName();
        String binaryDeployDir = tomcat.getParentFile().getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        if (tomcatDir != null && !tomcatDir.isEmpty()) {
            distributeBinary(hostname, tomcatDir, binaryDeployDir);
        } else {
            LOGGER.warn("Tomcat dir location is null or empty {}", tomcatDir);
        }
    }

    @Override
    public void distributeWebServer(final String hostname) {
        File apache = new File("remote.paths.apache.httpd");
        String webServerDir = apache.getName();
        String binaryDeployDir = apache.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        if (webServerDir != null && !webServerDir.isEmpty()) {
            distributeBinary(hostname, webServerDir, binaryDeployDir);
        } else {
            LOGGER.warn("WebServer dir location is null or empty {}", webServerDir);
        }
    }

    private void distributeBinary(final String hostname, final String binaryName, final String binaryDeployDir) {
        String binaryDir = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY);
        if (binaryDeployDir != null && !binaryDeployDir.isEmpty()) {
            if (!remoteFileCheck(hostname, binaryDeployDir + "/" + binaryName)) {
                LOGGER.info("Couldn't find {} on host {}. Trying to deploy it", binaryName, hostname);
                if (binaryDir != null && !binaryDir.isEmpty()) {
                    String zipFile = binaryDir + "/" + binaryName + ".zip";
                    String destinationZipFile = binaryDeployDir + "/" + binaryName + ".zip";
                    if (!new File(zipFile).exists()) {
                        LOGGER.debug("binary zip does not exists, create zip");
                        zipFile = zipBinary(binaryDir + "/" + binaryName);
                    }
                    if (remoteCreateDirectory(hostname, binaryDeployDir)) {
                        LOGGER.info("successfully created directories {}", binaryDeployDir);
                        if (remoteSecureCopyFile(hostname, zipFile, destinationZipFile)) {
                            LOGGER.info("successfully copied the binary {} over to {}", zipFile, destinationZipFile);
                            try {
                                if (remoteUnzipBinary(hostname, destinationZipFile, binaryDeployDir)) {
                                    LOGGER.info("successfully unzipped the binary {}", destinationZipFile);
                                } else {
                                    final String message = "JDK not found at " +  ApplicationProperties.get("stp.java.home");
                                    LOGGER.error(message);
                                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
                                }
                            } finally {
                                if (remoteDeleteBinary(hostname, destinationZipFile)) {
                                    LOGGER.info("successfully delete the binary {}", destinationZipFile);
                                } else {
                                    final String message = "error in deleting file " + destinationZipFile;
                                    LOGGER.error(message);
                                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
                                }
                            }
                        } else {
                            final String message = "error with scp of binary " + zipFile + " to destination " + destinationZipFile;
                            LOGGER.error(message);
                            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
                        }
                    } else {
                        final String message = "user does not have permission to create the directory " + binaryDeployDir;
                        LOGGER.error(message);
                        throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
                    }
                } else {
                    LOGGER.warn("Cannot find the binary directory location in jwala, value is {}", binaryDir);
                }
            } else {
                LOGGER.info("Found {} at on host {}", binaryName, hostname);
            }
        } else {
            LOGGER.warn("Binary deploy location not provided value is {}", binaryDeployDir);
        }
    }

    private boolean remoteDeleteBinary(final String hostname, final String destination) {
        boolean result;
        try {
            result = binaryDistributionControlService.deleteBinary(hostname, destination).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in delete remote binary at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        return result;
    }

    private boolean remoteUnzipBinary(final String hostname, final String binaryLocation, final String destination) {
        boolean result;
        try {
            result = binaryDistributionControlService.unzipBinary(hostname, binaryLocation, destination).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in remote unzip binary at host: " + hostname + " binaryLocation: " + binaryLocation + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        return result;
    }

    private boolean remoteSecureCopyFile(final String hostname, final String source, final String destination) {
        boolean result;
        try {
            result = binaryDistributionControlService.secureCopyFile(hostname, source, destination).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in remote secure copy at host: " + hostname + " source: " + source + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        return result;
    }

    private boolean remoteCreateDirectory(final String hostname, final String destination) {
        boolean result;
        try {
            result = binaryDistributionControlService.createDirectory(hostname, destination).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in create remote directory at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        return result;
    }

    private boolean remoteFileCheck(final String hostname, final String destination) {
        boolean result;
        try {
            result = binaryDistributionControlService.checkFileExists(hostname, destination).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in check remote File at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        return result;
    }

    @Override
    public String zipBinary(final String location) {
        String destination = null;
        if (location != null && !location.isEmpty() && new File(location).exists()) {
            destination = location + ".zip";
            LOGGER.info("{} found, zipping to {}", location, destination);
            zipDirectory.zip(location, destination);
        } else {
            LOGGER.warn("Could not find the location {}", location);
        }
        return destination;
    }
}
