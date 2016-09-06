package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.jvm.JvmControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public void distributeJdk(String hostname) {

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
    public void zipJdk() {
        String jdkLocation = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY) + "/" + ApplicationProperties.get(JDK_PROPERTY_KEY);
    }

    @Override
    public void zipTomcat() {
        String tomcatLocation = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY) + "/" + ApplicationProperties.get(TOMCAT_PROPERTY_KEY);
    }

    @Override
    public void zipWebServer() {
        String webServerLocation = ApplicationProperties.get(BINARY_LOCATION_PROPERTY_KEY) + "/" + ApplicationProperties.get(WEBSERVER_PROPERTY_KEY);
    }
}
