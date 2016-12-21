package com.cerner.jwala.common;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A utility class for file related operations
 *
 * Created by Jedd Anthony Cuison on 12/1/2016
 */
@Component
public class FileUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtility.class);
    /**
     * Unzips the file to the specified destination
     * @param destination the destination e.g. c:/scratch
     */
    public void unzip(final File zipFile, final File destination) {

        if (!destination.exists() && !destination.mkdir()) {
            throw new FileUtilityException("Failed to create zip file destination directory \"" + destination + "\"!");
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(zipFile);
            final Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry jarEntry = (JarEntry) entries.nextElement();
                final File f = new File(destination + File.separator + jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    boolean created = f.mkdir();
                    continue;
                }
                final InputStream in = jarFile.getInputStream(jarEntry);
                final FileOutputStream fos = new FileOutputStream(f);
                while (in.available() > 0) {
                    fos.write(in.read());
                }
                fos.close();
                in.close();
            }
        } catch (final IOException e) {
            throw new FileUtilityException("Failed to unpack " + zipFile.getAbsolutePath() + "!", e);
        }finally {
            if(jarFile!=null){
                try {
                    jarFile.close();
                }catch (IOException ioe){
                    LOGGER.info("Error closing jar",ioe);
                }
            }

        }
    }

}
