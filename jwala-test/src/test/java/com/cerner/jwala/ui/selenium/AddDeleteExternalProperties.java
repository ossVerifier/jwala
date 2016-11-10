package com.cerner.jwala.ui.selenium;

import com.cerner.jwala.ui.selenium.util.SeleniumTestCaseUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * Created on 11/9/2016.
 */
public class AddDeleteExternalProperties {
    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private InputStream inputStream;
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        inputStream = System.getProperty("selenium.property.file") == null ?
                ClassLoader.getSystemResourceAsStream("test.properties") : new FileInputStream(System.getProperty("selenium.property.file"));
        properties.load(inputStream);
        System.setProperty(properties.getProperty("webdriver.name"), properties.getProperty("webdriver.value"));
        driver = (WebDriver) Class.forName(properties.getProperty("webdriver.class")).getConstructor().newInstance();
        baseUrl = properties.getProperty("jwala.base.url");
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void testResourceExternalPropertiesUploadAndDelete() throws Exception {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.password"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div")))
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }

        SeleniumTestCaseUtility.waitABit();

        driver.findElement(By.linkText("Configuration")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.linkText("JVM")))
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }

        SeleniumTestCaseUtility.waitABit();

        driver.findElement(By.linkText("Resources")).click();
        driver.findElement(By.xpath("//div[2]/div/span")).click();

        SeleniumTestCaseUtility.waitABit();

        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-plusthick")).click();
        driver.findElement(By.name("templateFile")).clear();
        driver.findElement(By.name("templateFile")).sendKeys("D:\\jwala-resources\\external.properties");
        driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//span[text()=\"ext.properties\"]"))) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }

        driver.findElement(By.xpath("//span[text()=\"ext.properties\"]")).click();
        driver.findElement(By.cssSelector("input.noSelect")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-trash")).click();
        driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }
}
