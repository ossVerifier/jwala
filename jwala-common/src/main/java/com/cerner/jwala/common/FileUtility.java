package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.GZIPInputStream;

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
            throw new FileUtilityException("Failed to create zip file destination directory \"" + destination.getAbsolutePath() + "\"!");
        }
        long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Start unzip {}", zipFile.getAbsoluteFile());
            final JarFile jarFile = new JarFile(zipFile);
            final Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry jarEntry = (JarEntry) entries.nextElement();
                final File f = new File(destination + File.separator + jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    if (!f.mkdir()) {
                        throw new ApplicationException("File to create directory " + jarEntry.getName());
                    }
                    continue;
                }
                final InputStream in = jarFile.getInputStream(jarEntry);
                final FileOutputStream fos = new FileOutputStream(f);
                BufferedOutputStream bufout = new BufferedOutputStream(fos);

                while (in.available() > 0) {
                    bufout.write(in.read());
                }
                bufout.close();
                bufout.close();
                in.close();
            }
        } catch (final IOException e) {
            throw new FileUtilityException("Failed to unpack " + zipFile.getAbsolutePath() + "!", e);
        }
        LOGGER.debug("End unzip {} in {} ms", zipFile.getAbsoluteFile(), (System.currentTimeMillis() - startTime));
    }

    public void createJarArchive(File archiveFile, File[] filesToBeJared, String parent) {

        final int BUFFER_SIZE = 10240;
        try (
                FileOutputStream stream = new FileOutputStream(archiveFile);
                JarOutputStream out = new JarOutputStream(stream, new Manifest())) {

            byte buffer[] = new byte[BUFFER_SIZE];

            // Open archive file
            for (File aFileTobeJared : filesToBeJared) {
                if (aFileTobeJared == null || !aFileTobeJared.exists() || aFileTobeJared.isDirectory()) {
                    continue; // Just in case...
                }

                LOGGER.debug("Adding " + aFileTobeJared.getPath());

                File parentDir = new File(parent);

                String relPath = aFileTobeJared.getCanonicalPath()
                        .substring(parentDir.getParentFile().getCanonicalPath().length() + 1,
                                aFileTobeJared.getCanonicalPath().length());

                relPath = relPath.replace("\\", "/");

                // Add archive entry
                JarEntry jarAdd = new JarEntry(relPath);
                jarAdd.setTime(aFileTobeJared.lastModified());
                out.putNextEntry(jarAdd);
                BufferedOutputStream bout = new BufferedOutputStream(out);

                // Write file to archive
                try (FileInputStream in = new FileInputStream(aFileTobeJared)) {
                    while (true) {
                        int nRead = in.read(buffer, 0, buffer.length);
                        if (nRead <= 0)
                            break;
                        bout.write(buffer, 0, nRead);
                    }
                }
            }

            LOGGER.debug("Adding files to jar completed");
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

    }

    /**
     * Unzips the file to the specified destination
     * @param destination the destination e.g. c:/scratch
     */
    public void unGzip(final File gZipFile, final File destination) {

        if (!destination.exists() && !destination.mkdir()) {
            throw new FileUtilityException("Failed to create zip file destination directory \"" + destination.getAbsolutePath() + "\"!");
        }
        try {
            byte[] buffer = new byte[1024];
            final GZIPInputStream gzis  = new  GZIPInputStream(new FileInputStream(gZipFile));
            FileOutputStream out = new FileOutputStream(destination);
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzis.close();
            out.close();

        } catch (final Throwable e) {
            throw new FileUtilityException("Failed to unpack " + gZipFile.getAbsolutePath() + "!", e);
        }
    }
}