package com.cerner.jwala.ui.selenium;

import org.openqa.selenium.WebDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created on 11/9/2016.
 */
public class SeleniumTestCase {

    // setup properties
    private static final String PROPERTY_JWALA_BASE_URL = "jwala.base.url";
    private static final String PROPERTY_JWALA_WAIT_BETWEEN_STEPS_TIME_MS = "jwala.wait.between.steps.time.ms";
    private static final String PROPERTY_JWALA_WAIT_BETWEEN_STEPS = "jwala.wait.between.steps";
    private static final String PROPERTY_SELENIUM_PROPERTY_FILE = "selenium.property.file";
    private static final String PROPERTY_WEBDRIVER_NAME = "webdriver.name";
    private static final String PROPERTY_WEBDRIVER_VALUE = "webdriver.value";
    private static final String PROPERTY_WEBDRIVER_CLASS = "webdriver.class";

    private static final String JWALA_SELENIUM_TEST_PROPERTIES = "jwala-selenium-test.properties";

    // shared properties
    protected static final String PROPERTY_JWALA_RESOURCES_UPLOAD_DIR = "jwala.resources.upload.dir";
    protected static final String PROPERTY_JWALA_PATH_SEPARATOR = "jwala.path.separator";

    protected static final String EXTERNAL_PROPERTIES_FILE_NAME = "external.properties";

    protected Properties properties;
    protected WebDriver driver;
    protected String baseUrl;

    public void setUpSeleniumDrivers() throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        properties = new Properties();
        final String propertyFile = System.getProperty(PROPERTY_SELENIUM_PROPERTY_FILE);
        try (InputStream inputStream = (propertyFile == null || propertyFile.isEmpty()) ?
                ClassLoader.getSystemResourceAsStream(JWALA_SELENIUM_TEST_PROPERTIES) : new FileInputStream(propertyFile)) {
            properties.load(inputStream);
        }
        System.setProperty(properties.getProperty(PROPERTY_WEBDRIVER_NAME), properties.getProperty(PROPERTY_WEBDRIVER_VALUE));
        driver = (WebDriver) Class.forName(properties.getProperty(PROPERTY_WEBDRIVER_CLASS)).getConstructor().newInstance();
        baseUrl = properties.getProperty(PROPERTY_JWALA_BASE_URL);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }


    public void waitABit() throws IOException, InterruptedException {
        final Boolean isWaitPropertySet = Boolean.valueOf(properties.getProperty(PROPERTY_JWALA_WAIT_BETWEEN_STEPS, "false"));
        if (isWaitPropertySet){
            final long sleepTime = Long.parseLong(properties.getProperty(PROPERTY_JWALA_WAIT_BETWEEN_STEPS_TIME_MS, "1000"));
            Thread.sleep(sleepTime);
        }

    }



}
