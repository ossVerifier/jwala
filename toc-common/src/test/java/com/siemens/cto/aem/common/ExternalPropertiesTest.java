package com.siemens.cto.aem.common;

import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.properties.ExternalProperties;
import junit.framework.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ExternalPropertiesTest extends TestCase {

    public static final String SRC_TEST_RESOURCES_PROPERTIES = new File(".").getAbsolutePath() + "/src/test/resources/properties/";
    public static final String EXTERNAL_PROPERTIES = "external.properties";

    public void testBadPropertiesPath() {
        try {
            ExternalProperties.setPropertiesFilePath(SRC_TEST_RESOURCES_PROPERTIES + "NOPE.properties");
            ExternalProperties.get("doesn't matter");
        } catch (ApplicationException e) {
            assertTrue(true);
            return;
        }

        assertFalse(false);
    }

    public void testLoadProperties() {
        ExternalProperties.setPropertiesFilePath(null);
        assertEquals(0, ExternalProperties.size());

        ExternalProperties.setPropertiesFilePath(SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);
        assertTrue(ExternalProperties.size() > 0);
    }

    public void testReadProperties() {
        ExternalProperties.setPropertiesFilePath(SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);
        assertEquals("string property", ExternalProperties.get("string.property"));
        assertEquals(Integer.valueOf(5), ExternalProperties.getAsInteger("integer.property"));
        assertEquals(Boolean.TRUE, ExternalProperties.getAsBoolean("boolean.property"));
    }

    public void testReload() throws IOException {
        ExternalProperties.setPropertiesFilePath(SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);

        final String propertyToAdd = "test.reload=true";
        writeNewPropertyToFile(propertyToAdd, SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);

        ExternalProperties.reload();
        assertEquals("string property", ExternalProperties.get("string.property"));
        assertEquals(Integer.valueOf(5), ExternalProperties.getAsInteger("integer.property"));
        assertEquals(Boolean.TRUE, ExternalProperties.getAsBoolean("boolean.property"));
        assertEquals(Boolean.TRUE, ExternalProperties.getAsBoolean("test.reload"));
        assertNull(ApplicationProperties.get("home team"));

        deleteLineFromFile(propertyToAdd, SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);
    }

    private void writeNewPropertyToFile(String propertyToAdd, String propertiesFilePath) throws IOException {
        Files.write(Paths.get(propertiesFilePath), propertyToAdd.getBytes(), StandardOpenOption.APPEND);
    }

    private void deleteLineFromFile(String lineToDelete, String propertiesFilePath) throws IOException {
        File inputFile = new File(propertiesFilePath);

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String lineToRemove;
        lineToRemove = lineToDelete;
        String currentLine = reader.readLine();
        StringBuffer fileContent = new StringBuffer();

        while (currentLine != null) {
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if (!trimmedLine.equals(lineToRemove)) {
                fileContent.append(currentLine);
            }

            currentLine = reader.readLine();
            if (currentLine != null) {
                fileContent.append(System.getProperty("line.separator"));
            }
        }
        reader.close();

        try (PrintWriter printWriter = new PrintWriter(inputFile)) {
            printWriter.print(fileContent);
        }
    }
}
