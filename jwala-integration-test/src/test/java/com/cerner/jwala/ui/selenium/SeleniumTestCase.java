package com.cerner.jwala.ui.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * Created on 11/9/2016
 */
public class SeleniumTestCase {

    // setup properties
    private static final String PROPERTY_JWALA_BASE_URL = ApplicationProperties.get(PropertyKeys.JWALA_BASE_URL);
    private static final String PROPERTY_JWALA_WAIT_BETWEEN_STEPS_TIME_MS = "jwala.wait.between.steps.time.ms";
    private static final String PROPERTY_JWALA_WAIT_BETWEEN_STEPS = ApplicationProperties.get(PropertyKeys.JWALA_WAIT_BETWEEN_STEPS);
    private static final String PROPERTY_SELENIUM_PROPERTY_FILE = "selenium.property.file";
    private static final String PROPERTY_WEBDRIVER_NAME = ApplicationProperties.get(PropertyKeys.WEBDRIVER_NAME);
    private static final String PROPERTY_WEBDRIVER_VALUE = ApplicationProperties.get(PropertyKeys.WEBDRIVER_VALUE);
    private static final String PROPERTY_WEBDRIVER_CLASS = ApplicationProperties.get(PropertyKeys.WEBDRIVER_CLASS);

    private static final String JWALA_SELENIUM_TEST_PROPERTIES = "selenium/jwala-selenium-test.properties";

    // shared properties
    protected static final String PROPERTY_JWALA_RESOURCES_UPLOAD_DIR = ApplicationProperties.get(PropertyKeys.JWALA_RESOURCES_UPLOAD_DIR);
    protected static final String PROPERTY_JWALA_PATH_SEPARATOR = ApplicationProperties.get(PropertyKeys.JWALA_PATH_SEPARATOR);

    protected static final String EXTERNAL_PROPERTIES_FILE_NAME = "external.properties";

    protected Properties properties;
    protected WebDriver driver;
    protected String baseUrl;



    public void setUpSeleniumDrivers() {
        properties = new Properties();

        final String propertyFile = System.getProperty(PROPERTY_SELENIUM_PROPERTY_FILE);

        try (final InputStream in = (propertyFile == null || propertyFile.isEmpty()) ? loadPropertiesFromResource()
                : new FileInputStream(propertyFile)) {
            properties.load(in);
        } catch (IOException e) {
            throw new SeleniumTestCaseException(e);
        }

        System.setProperty(properties.getProperty(PROPERTY_WEBDRIVER_NAME), properties.getProperty(PROPERTY_WEBDRIVER_VALUE));

        try {
            driver = (WebDriver) Class.forName(properties.getProperty(PROPERTY_WEBDRIVER_CLASS)).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new SeleniumTestCaseException(e);
        }

        baseUrl = properties.getProperty(PROPERTY_JWALA_BASE_URL);

        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    /**
     * Load properties from resource as stream
     * @return properties data as input stream
     */
    private InputStream loadPropertiesFromResource() {
        return this.getClass().getClassLoader().getResourceAsStream(JWALA_SELENIUM_TEST_PROPERTIES);
    }

    public void waitABit() throws IOException, InterruptedException {
        final Boolean isWaitPropertySet = Boolean.valueOf(properties.getProperty(PROPERTY_JWALA_WAIT_BETWEEN_STEPS, "false"));
        if (isWaitPropertySet){
            final long sleepTime = Long.parseLong(properties.getProperty(PROPERTY_JWALA_WAIT_BETWEEN_STEPS_TIME_MS, "1000"));
            Thread.sleep(sleepTime);
        }
    }

    protected void login() throws InterruptedException {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty(ApplicationProperties.get(PropertyKeys.JWALA_USERNAME)));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty(ApplicationProperties.get(PropertyKeys.JWALA_PASSWORD)));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div"))) break;
            Thread.sleep(1000);
        }
    }

    protected boolean isElementPresent(final By by) {
        try {
            final WebElement webElement = driver.findElement(by);
            return webElement != null;
        } catch (final NoSuchElementException e) {
            return false;
        }
    }

}
