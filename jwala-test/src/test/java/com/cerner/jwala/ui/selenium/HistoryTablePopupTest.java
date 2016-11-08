package com.cerner.jwala.ui.selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HistoryTablePopupTest {
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
    public void testHistoryTablePopup() throws Exception {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.password"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div"))) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.id("group-operations-table_1")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.xpath("//div[@id='ext-comp-div-group-operations-table_1']/div/div/div"))) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-newwin")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.cssSelector("span.ui-dialog-title.text-align-center"))) break;
            Thread.sleep(1000);
        }

        try {
            assertTrue(isElementPresent(By.cssSelector("span.ui-dialog-title.text-align-center")));
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
        driver.findElement(By.xpath("(//button[@type='button'])[5]")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("HEALTH CHECK 4.0".equals(driver.findElement(By.cssSelector("tr.even > td.adj-col.sorting_1")).getText())) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.linkText("Logout")).click();
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
