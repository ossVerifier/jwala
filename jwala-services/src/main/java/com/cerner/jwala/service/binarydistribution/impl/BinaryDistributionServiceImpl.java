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
    private static final String JDK_PROPERTY_KEY = "jwala.binary.dir.jdk";
    private static final String TOMCAT_PROPERTY_KEY = "jwala.binary.dir.tomcat";
    private static final String WEBSERVER_PROPERTY_KEY = "jwala.binary.dir.webServer";
    private static final String BINARY_DEPLOY_LOCATION_PROPERTY_KEY = "jwala.binary.deploy.dir";

    private final ZipDirectory zipDirectory = new ZipDirectoryImpl();
    private final BinaryDistributionControlService binaryDistributionControlService;

    @Autowired
    public BinaryDistributionServiceImpl(BinaryDistributionControlService binaryDistributionControlService) {
        this.binaryDistributionControlService = binaryDistributionControlService;
    }

    @Override
    public void distributeJdk(final String hostname) {
        String jdkDir = ApplicationProperties.get(JDK_PROPERTY_KEY);
        if (jdkDir != null && !jdkDir.isEmpty()) {
            distributeBinary(hostname, jdkDir);
        } else {
            LOGGER.warn("JDK dir location is null or empty {}", jdkDir);
        }
    }

    @Override
    public void distributeTomcat(final String hostname) {
        String tomcatDir = ApplicationProperties.get(TOMCAT_PROPERTY_KEY);
        if (tomcatDir != null && !tomcatDir.isEmpty()) {
            distributeBinary(hostname, tomcatDir);
        } else {
            LOGGER.warn("Tomcat dir location is null or empty {}", tomcatDir);
        }
    }

    @Override
    public void distributeWebServer(final String hostname) {
        String webServerDir = ApplicationProperties.get(WEBSERVER_PROPERTY_KEY);
        if (webServerDir != null && !webServerDir.isEmpty()) {
            distributeBinary(hostname, webServerDir);
        } else {
            LOGGER.warn("WebServer dir location is null or empty {}", webServerDir);
        }
    }

    private void distributeBinary(final String hostname, final String binaryName) {
        String binaryDir = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY);
        String binaryDeployDir = ApplicationProperties.get(BINARY_DEPLOY_LOCATION_PROPERTY_KEY);
        if (binaryDeployDir != null && !binaryDeployDir.isEmpty()) {
            try {
                if (!binaryDistributionControlService.checkFileExists(binaryDeployDir + "/" + binaryName, hostname).getReturnCode().wasSuccessful()) {
                    LOGGER.info("Couldn't find {} on host {}. Trying to deploy it", binaryName, hostname);
                    if (binaryDir != null && !binaryDir.isEmpty()) {
                        String zipFile = binaryDir + "/" + binaryName + ".zip";
                        String destinationZipFile = binaryDeployDir + "/" + binaryName + ".zip";
                        if (!new File(zipFile).exists()) {
                            LOGGER.debug("binary zip does not exists, create zip");
                            zipFile = zipBinary(binaryDir + "/" + binaryName);
                        }
                        if (binaryDistributionControlService.createDirectory(hostname, binaryDeployDir).getReturnCode().wasSuccessful()) {
                            LOGGER.info("successfully created directories {}", binaryDeployDir);
                            if (binaryDistributionControlService.secureCopyFile(hostname, zipFile, destinationZipFile).getReturnCode().wasSuccessful()) {
                                if (binaryDistributionControlService.unzipBinary(hostname, destinationZipFile, binaryDeployDir + "/" + binaryName).getReturnCode().wasSuccessful()) {
                                    LOGGER.info("successfully unzipped the binary {}", destinationZipFile);
                                    if (binaryDistributionControlService.deleteBinary(hostname, destinationZipFile).getReturnCode().wasSuccessful()) {
                                        LOGGER.info("successfully delete the binary {}", destinationZipFile);
                                    } else {
                                        LOGGER.error("Issue with deleting binary {}", destinationZipFile);
                                    }
                                } else {
                                    LOGGER.error("Issue with unzipping file {}", destinationZipFile);
                                }
                            } else {
                                LOGGER.error("Issue with creating directories {}", binaryDeployDir);
                            }
                        }
                    } else {
                        LOGGER.warn("Cannot find the binary directory location in jwala, value is {}", binaryDir);
                    }
                } else {
                    LOGGER.info("Found {} at on host {}", binaryName, hostname);
                }
            } catch (CommandFailureException e) {
                LOGGER.error("Error with copying binary for " + binaryName, e);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error with copying binary for " + binaryName, e);
            }
        } else {
            LOGGER.warn("Binary deploy location not provided value is {}", binaryDeployDir);
        }
    }

    @Override
    public boolean jdkExists(final String hostname) {
        boolean result;
        try {
            result = binaryDistributionControlService.checkFileExists(hostname,
                    ApplicationProperties.get(BINARY_DEPLOY_LOCATION_PROPERTY_KEY) + "/" + ApplicationProperties.get(JDK_PROPERTY_KEY))
                    .getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            LOGGER.error("Error in checking if jdk exists", e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error with checking if jdk exists", e);
        }
        return result;
    }

    @Override
    public boolean tomcatExists(final String hostname) {
        boolean result;
        try {
            result = binaryDistributionControlService.checkFileExists(hostname,
                    ApplicationProperties.get(BINARY_DEPLOY_LOCATION_PROPERTY_KEY) + "/" + ApplicationProperties.get(TOMCAT_PROPERTY_KEY))
                    .getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            LOGGER.error("Error in checking if tomcat exists", e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error with checking if tomcat exists", e);
        }
        return result;
    }

    @Override
    public boolean webServerExists(final String hostname) {
        boolean result;
        try {
            result = binaryDistributionControlService.checkFileExists(hostname,
                    ApplicationProperties.get(BINARY_DEPLOY_LOCATION_PROPERTY_KEY) + "/" + ApplicationProperties.get(JDK_PROPERTY_KEY))
                    .getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            LOGGER.error("Error in checking if httpd exists", e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Error with checking if httpd exists", e);
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
