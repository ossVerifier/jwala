package com.cerner.jwala.ui.selenium.configuration.resources;

import com.cerner.jwala.ui.selenium.SeleniumTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Created on 11/14/2016.
 */
public class UploadModifySaveDeleteExternalPropertiesTest extends SeleniumTestCase {
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        setUpSeleniumDrivers();
    }

    @Test
    public void testResourceExternalPropertiesUploadModifySaveDelete() throws Exception {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.password"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        driver.findElement(By.linkText("Configuration")).click();
        driver.findElement(By.linkText("Resources")).click();
        driver.findElement(By.xpath("//span[text()=\"Ext Properties\"]")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-plusthick")).click();
        driver.findElement(By.name("templateFile")).clear();
        driver.findElement(By.name("templateFile")).sendKeys("D:\\jwala-resources\\external.properties");
        driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//span[text()=\"ext.properties\"]"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        driver.findElement(By.xpath("//span[text()=\"ext.properties\"]")).click();
        driver.findElement(By.cssSelector("li.ui-state-active > span")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-icon.ui-icon-disk"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        assertTrue(driver.findElement(By.cssSelector(".CodeMirror-line > span")).isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".CodeMirror-line > span")).isEnabled());

        driver.findElement(By.cssSelector(".CodeMirror-line > span")).click();
        driver.findElement(By.cssSelector(".CodeMirror-line > span")).sendKeys("selenium.test.property=running selenium tests ${vars['AemSsh.userName']}");
        assertEquals("selenium.test.property=running selenium tests ${vars['AemSsh.userName']}", driver.findElement(By.cssSelector("pre..CodeMirror-line")).getText());
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-disk")).click();
        driver.findElement(By.linkText("Template Preview")).click();
        driver.findElement(By.xpath("//span[text()=\"Ext Properties\"]")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-plusthick")).click();
        assertEquals("Only one external properties file can be uploaded. Any existing ones will be overwritten.", driver.findElement(By.cssSelector("span.msg")).getText());
        driver.findElement(By.name("templateFile")).clear();
        driver.findElement(By.name("templateFile")).sendKeys("D:\\jwala-resources\\external.properties");
        driver.findElement(By.xpath("(//span[text()=\"Ok\"])")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (!isElementPresent(By.xpath("//input[@name='templateFile']"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        driver.findElement(By.cssSelector("input.noSelect")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-trash")).click();
        driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
        assertFalse(isElementPresent(By.xpath("//span[text()=\"ext.properties\"]")));
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
