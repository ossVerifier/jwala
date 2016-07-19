package com.siemens.cto.aem.common.properties;

import com.siemens.cto.aem.common.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ExternalProperties {

    private volatile Properties properties;

    private static volatile ExternalProperties SELF;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProperties.class);

    public static String PROPERTIES_FILE_PATH = null;

    private ExternalProperties() {
        properties = new Properties();
        init();
    }

    public static ExternalProperties getInstance() {
        if (SELF == null) {
            synchronized (ExternalProperties.class) {
                if (SELF == null) {
                    SELF = new ExternalProperties();
                }
            }
        }

        return SELF;
    }

    public static Properties getProperties() {
        final Properties copy = new Properties();
        copy.putAll(getInstance().properties);
        return copy;
    }

    public static void setPropertiesFilePath(String propertiesFilePath){
        PROPERTIES_FILE_PATH = propertiesFilePath;
        reload();
    }

    public static void reload() {
        getInstance().init();
    }

    public static String get(String key) {
        String propVal = getProperties().getProperty(key);
        LOGGER.debug("PropertyGet(" + key + ")=(" + propVal + ")");
        return propVal;
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

    private void init() {
        String propertiesFile = PROPERTIES_FILE_PATH != null ? PROPERTIES_FILE_PATH : null;
        Properties tempProperties = new Properties();
        if (propertiesFile != null) {
            try {
                tempProperties.load(new FileReader(new File(propertiesFile)));
            } catch (IOException e) {
                throw new ApplicationException("Failed to load properties file " + propertiesFile, e);
            }
            LOGGER.info("Properties loaded from path " + propertiesFile);
        }
        properties = tempProperties;
    }

    public static String get(String key, String defaultValue) {
        String result = getProperties().getProperty(key);
        if (result == null) {
            return defaultValue;
        } else {
            return result;
        }
    }

}
