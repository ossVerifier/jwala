package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.zip.ZipDirectory;
import com.cerner.jwala.service.zip.impl.ZipDirectoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by SP043299 on 9/6/2016.
 */
public class BinaryDistributionServiceImpl implements BinaryDistributionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionServiceImpl.class);

    private final JvmControlService jvmControlService = null;
    private static final String BINARY_LOCATION_PROPERTY_KEY = "jwala.binary.dir";
    private static final String JDK_PROPERTY_KEY = "jwala.binary.dir.jdk";
    private static final String TOMCAT_PROPERTY_KEY = "jwala.binary.dir.tomcat";
    private static final String WEBSERVER_PROPERTY_KEY = "jwala.binary.dir.webServer";
    private static final String BINARY_DEPLOY_LOCATION_PROPERTY_KEY = "jwala.binary.deploy.dir";

    private final ZipDirectory zipDirectory = new ZipDirectoryImpl();

    @Override
    public void distributeJdk(String hostname) {
        // Check if binary exists at the destination
        // If exists, exit
        // If does not exists, check if zip exists in Jwala
        // If yes, scp over to destination
        // If no, zipBinary from BINARY_LOCATION_PROPERTY_KEY + JDK_PROPERTY_KEY
        // Get zip file and scp over
        // unzip binary
        // delete zip
    }

    @Override
    public void distributeTomcat(String hostname) {
    }

    @Override
    public void distributeWebServer(String hostname) {
    }

    @Override
    public boolean jdkExists(String hostname) {
        return false;
    }

    @Override
    public boolean tomcatExists(String hostname) {
        return false;
    }

    @Override
    public boolean webServerExists(String hostname) {
        return false;
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
