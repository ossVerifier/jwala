package com.siemens.cto.aem.common.properties;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.common.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ApplicationProperties {

    private Properties properties;

    private static volatile ApplicationProperties self = null;

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationProperties.class);

    public static ApplicationProperties getInstance() {
        if (self == null) {
            synchronized (ApplicationProperties.class) {
                if (self == null) {
                    self = new ApplicationProperties();
                }
            }
        }

        return self;
    }

    public static Properties getProperties() {
        return getInstance().properties;
    }

    public static void reload() {
        getInstance().init();
    }

    public static String get(String key) {
        return getProperties().getProperty(key);
    }

    public static Integer getAsInteger(String key) {
        return Integer.parseInt(getProperties().getProperty(key));
    }

    public static Boolean getAsBoolean(String key) {
        return Boolean.parseBoolean(getProperties().getProperty(key));
    }

    public static int size() {
        return getProperties().size();
    }

    private ApplicationProperties() {
        properties = new Properties();
        init();
    }

    private void init() {
        String propertiesFile = System.getProperty(AemConstants.PROPERTIES_ROOT_PATH) + "/" + AemConstants.PROPERTIES_FILE_NAME;
        properties = new Properties();
        try {
            properties.load(new FileReader(new File(propertiesFile)));
        } catch (IOException e) {
            throw new ApplicationException("Failed to load properties file " + propertiesFile, e);
        }
        LOG.info("Properties loaded from path " + propertiesFile);
    }
}
