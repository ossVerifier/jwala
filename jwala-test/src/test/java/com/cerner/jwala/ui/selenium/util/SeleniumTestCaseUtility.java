package com.cerner.jwala.ui.selenium.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 11/9/2016.
 */
public class SeleniumTestCaseUtility {

    private static Properties properties;
    private static InputStream inputStream;

    public static void waitABit() throws IOException, InterruptedException {
        // TODO initialize this somewhere else
        if (properties == null) {
            properties = new Properties();
            inputStream = System.getProperty("selenium.property.file") == null ?
                    ClassLoader.getSystemResourceAsStream("test.properties") : new FileInputStream(System.getProperty("selenium.property.file"));
            properties.load(inputStream);
        }

        final Boolean isWaitPropertySet = Boolean.valueOf(properties.getProperty("jwala.wait.between.steps", "false"));
        if (isWaitPropertySet){
            final long sleepTime = Long.parseLong(properties.getProperty("jwala.wait.between.steps.time.ms", "1000"));
            Thread.sleep(sleepTime);
        }

    }

}
