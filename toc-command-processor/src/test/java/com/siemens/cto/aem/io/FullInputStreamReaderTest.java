package com.siemens.cto.aem.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FullInputStreamReaderTest {

    @Test
    public void testReadString() throws Exception {

        final String expectedData = buildLargeString(8192, "This is the text that I am expecting to read from the input stream.");
        final Charset characterSet = StandardCharsets.UTF_8;
        final ByteArrayInputStream input = new ByteArrayInputStream(expectedData.getBytes(characterSet));
        final FullInputStreamReader reader = new FullInputStreamReader(input);
        final String actualData = reader.readString(characterSet);

        assertEquals(expectedData,
                     actualData);
    }

    @Test
    public void testReadBytes() throws Exception {

        final int expectedLength = 10000;
        final byte[] expectedBytes = new byte[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            expectedBytes[i] = new Integer(i).byteValue();
        }
        final ByteArrayInputStream input = new ByteArrayInputStream(expectedBytes);
        final FullInputStreamReader reader = new FullInputStreamReader(input);
        final byte[] actualBytes = reader.readBytes();

        assertArrayEquals(expectedBytes,
                          actualBytes);
    }

    protected String buildLargeString(final int aMinimumSize,
                                      final String aFragment) {
        final StringBuilder builder = new StringBuilder(aMinimumSize);
        while (builder.length() < aMinimumSize) {
            builder.append(aFragment);
        }
        return builder.toString();
    }
}
