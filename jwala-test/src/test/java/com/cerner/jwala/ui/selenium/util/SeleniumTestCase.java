package com.cerner.jwala.ui.selenium.util;

import org.openqa.selenium.WebDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created on 11/9/2016.
 */
public class SeleniumTestCase {

    protected Properties properties;
    protected WebDriver driver;
    protected String baseUrl;

    public void setUpSeleniumDrivers() throws IOException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        properties = new Properties();
        final String propertyFile = System.getProperty("selenium.property.file");
        try (InputStream inputStream = (propertyFile == null || propertyFile.isEmpty()) ?
                ClassLoader.getSystemResourceAsStream("test.properties") : new FileInputStream(propertyFile)) {
            properties.load(inputStream);
        }
        System.setProperty(properties.getProperty("webdriver.name"), properties.getProperty("webdriver.value"));
        driver = (WebDriver) Class.forName(properties.getProperty("webdriver.class")).getConstructor().newInstance();
        baseUrl = properties.getProperty("jwala.base.url");
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }


    public void waitABit() throws IOException, InterruptedException {
        final Boolean isWaitPropertySet = Boolean.valueOf(properties.getProperty("jwala.wait.between.steps", "false"));
        if (isWaitPropertySet){
            final long sleepTime = Long.parseLong(properties.getProperty("jwala.wait.between.steps.time.ms", "1000"));
            Thread.sleep(sleepTime);
        }

    }



}
