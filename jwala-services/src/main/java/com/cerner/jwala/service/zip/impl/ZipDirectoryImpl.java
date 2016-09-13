package com.cerner.jwala.service.zip.impl;

import com.cerner.jwala.service.exception.ZipDirectoryException;
import com.cerner.jwala.service.zip.ZipDirectory;
import org.apache.openjpa.lib.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by SP043299 on 9/6/2016.
 */
public class ZipDirectoryImpl implements ZipDirectory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipDirectoryImpl.class);

    /**
     * This method zips the source directory into the destination directory.
     *
     * @param source This is the source directory location.
     * @param destination This is the destination file. The destination should contain .zip extension.
     */
    @Override
    public void zip(final String source, final String destination) {
        File zipDir = new File(source);
        if (zipDir.exists()) {
            if (new File(destination).exists()) {
                LOGGER.debug("Previous copy of the {} exists, backing it up", destination);
                String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
                try {
                    Files.copy(new File(destination), new File(destination + currentDateSuffix));
                } catch (IOException e) {
                    LOGGER.error("Error with backing up old zip file", e);
                    throw new ZipDirectoryException(e);
                }
            }
            LOGGER.debug("Zipping {} to {}", source, destination);
            ArrayList<String> fileNames = new ArrayList<>();
            getFiles(zipDir, source, fileNames);
            FileOutputStream fileOutputStream;
            ZipOutputStream zipOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(destination);
                zipOutputStream = new ZipOutputStream(fileOutputStream);

                byte[] buffer = new byte[1024];
                for (String fileName : fileNames) {
                    LOGGER.debug("Adding {} to {} zipfile", fileName, destination);
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipOutputStream.putNextEntry(zipEntry);

                    FileInputStream fileInputStream = new FileInputStream(source + "/" + fileName);
                    int length = 0;
                    while ((length = fileInputStream.read()) > 0) {
                        zipOutputStream.write(buffer, 0, length);
                    }
                    fileInputStream.close();
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Error with finding the file ", e);
                throw new ZipDirectoryException(e);
            } catch (IOException e) {
                LOGGER.error("Error with adding next zip entry", e);
                throw new ZipDirectoryException(e);
            } finally {
                if (zipOutputStream != null) {
                    try {
                        zipOutputStream.closeEntry();
                        zipOutputStream.close();
                    } catch (IOException e) {
                        LOGGER.error("Error in closing zipoutputstream", e);
                        throw new ZipDirectoryException(e);
                    }
                }
            }
        } else {
            LOGGER.warn("The {} directory does not exist, cannot zip it", source);
        }
    }

    /**
     * This method returns the relative path of all the files in the source directory.
     *
     * @param file The file/directory which needs to be added to the ArrayList
     * @param source The original source directory
     * @param files ArrayList of all the files under the source directory
     */
    private void getFiles(final File file, final String source, final ArrayList<String> files) {
        if (file.isFile()) {
            files.add(file.getAbsolutePath().substring(source.length()));
        } else if (file.isDirectory()) {
            for (String f : file.list()) {
                getFiles(new File(file, f), source, files);
            }
        }
    }
}
