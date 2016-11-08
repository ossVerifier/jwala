package com.cerner.jwala.ui.selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class AddDeleteJvmTest {
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();

  private Properties properties;

  @Before
  public void setUp() throws Exception {
    properties = new Properties();
    properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
    System.setProperty(properties.getProperty("webdriver.name"), properties.getProperty("webdriver.value"));
    driver = (WebDriver) Class.forName(properties.getProperty("webdriver.class")).getConstructor().newInstance();
    baseUrl = properties.getProperty("jwala.baseUrl");
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testAddDeleteJvm() throws Exception {
    driver.get(baseUrl + "/toc/login");
    driver.findElement(By.id("userName")).sendKeys("jwala");
    driver.findElement(By.id("password")).sendKeys("jwala");
    driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
    for (int second = 0;; second++) {
    	if (second >= 60) fail("timeout");
    	try {
          if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div")))
            break;
        } catch (Exception e) {
          e.printStackTrace();
        }
    	Thread.sleep(1000);
    }

    driver.findElement(By.linkText("Configuration")).click();
    for (int second = 0;; second++) {
    	if (second >= 60) fail("timeout");
    	try {
          if (isElementPresent(By.linkText("JVM")))
            break;
        } catch (Exception e) {
          e.printStackTrace();
        }
    	Thread.sleep(1000);
    }

    driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
    for (int second = 0;; second++) {
    	if (second >= 60) fail("timeout");
    	try {
          if (isElementPresent(By.cssSelector("span.ui-dialog-title.text-align-center")))
            break;
        } catch (Exception e) {
          e.printStackTrace();
        }
    	Thread.sleep(1000);
    }

    driver.findElement(By.name("jvmName")).sendKeys("test-jvm-1");
    driver.findElement(By.name("hostName")).sendKeys("localhost");
    driver.findElement(By.name("statusPath")).clear();
    driver.findElement(By.name("statusPath")).sendKeys("");
    driver.findElement(By.name("statusPath")).sendKeys("/tomcat-power.gif");
    driver.findElement(By.name("httpPort")).sendKeys("10000");
    driver.findElement(By.name("httpsPort")).sendKeys("10001");
    driver.findElement(By.name("redirectPort")).sendKeys("10002");
    driver.findElement(By.name("shutdownPort")).sendKeys("10003");
    driver.findElement(By.name("ajpPort")).sendKeys("10004");
    driver.findElement(By.name("groupSelector[]")).click();
    driver.findElement(By.xpath("(//button[@type='button'])[4]")).click();
    for (int second = 0;; second++) {
    	if (second >= 60) fail("timeout");
    	try { if ("test-jvm-1".equals(driver.findElement(By.xpath("//table[@id='jvm-config-datatable']/tbody/tr[7]/td")).getText())) break; } catch (Exception e) {}
    	Thread.sleep(1000);
    }

    driver.findElement(By.xpath("//table[@id='jvm-config-datatable']/tbody/tr[7]/td")).click();
    driver.findElement(By.xpath("//button[@type='button']")).click();
    for (int second = 0;; second++) {
    	if (second >= 60) fail("timeout");
    	try {
          if (isElementPresent(By.cssSelector("span.ui-dialog-title.text-align-center")))
            break;
        } catch (Exception e) {
          e.printStackTrace();
        }
    	Thread.sleep(1000);
    }

    driver.findElement(By.xpath("(//button[@type='button'])[4]")).click();
    for (int second = 0;; second++) {
    	if (second >= 60) fail("timeout");
    	try {
          if (isElementPresent(By.xpath("//div[@id='jvm-config-datatable_wrapper']/div")))
            break;
        } catch (Exception e) {
          e.printStackTrace();
        }
    	Thread.sleep(1000);
    }

    driver.findElement(By.linkText("Operations")).click();
    for (int second = 0;; second++) {
    	if (second >= 60) fail("timeout");
    	try {
          if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div")))
            break;
        } catch (Exception e) {
          e.printStackTrace();
        }
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
