package com.cerner.jwala.service.zip;

import com.cerner.jwala.service.zip.impl.ZipDirectoryImpl;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by SP043299 on 9/6/2016.
 */
public class ZipDirectoryImplTest {
    public ZipDirectory zipDirectory = new ZipDirectoryImpl();

    @Test
    public void testZipDirectory() {
        String source = getClass().getClassLoader().getResource("zip-test").getFile();
        String destination = source + ".zip";
        zipDirectory.zip(source, destination);
        assertTrue(new File(destination).exists());
    }

    @Test
    public void testMissingZipDirectory() {
        String source = getClass().getClassLoader().getResource("zip-test").getFile();
        source += "/test";
        String destination = source + ".zip";
        zipDirectory.zip(source, destination);
        assertFalse(new File(destination).exists());
    }

    @Test (expected = StringIndexOutOfBoundsException.class)
    public void testSingleZipFile() {
        String source = getClass().getClassLoader().getResource("zip-test/test.txt").getFile();
        String destination = source + ".zip";
        zipDirectory.zip(source, destination);
    }
}
