package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

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

        try {
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
                while (in.available() > 0) {
                    fos.write(in.read());
                }
                fos.close();
                in.close();
            }
        } catch (final IOException e) {
            throw new FileUtilityException("Failed to unpack " + zipFile.getAbsolutePath() + "!", e);
        }
    }

    /**
     * Gets the parent directory of the first path entry of a zip file e.g. entries = [folder1/file1, folder1/folder2]
     * returns "folder"
     * @param zipFilename the zip filename
     * @return the parent of the first path entry
     */
    public String getFirstZipEntryParent(final String zipFilename) {
        try {
            final ZipFile zipFile = new ZipFile(zipFilename);
            final Enumeration zipEntryEnumeration = zipFile.entries();
            if (zipEntryEnumeration.hasMoreElements()) {
                final ZipEntry zipEntry = (ZipEntry) zipEntryEnumeration.nextElement();
                zipFile.close();
                return zipEntry.getName().substring(0, zipEntry.getName().indexOf("/"));
            }
            zipFile.close();
            return null;
        } catch (final IOException e) {
            throw new FileUtilityException(MessageFormat.format("Failed to get {0} parent path!", zipFilename), e);
        }
    }

    public static void main(String [] args) throws IOException {
        final FileUtility fileUtility = new FileUtility();
        System.out.println(fileUtility.getFirstZipEntryParent("D:/scratch/jedd2.zip"));
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


                // Write file to archive
                try (FileInputStream in = new FileInputStream(aFileTobeJared)) {
                    while (true) {
                        int nRead = in.read(buffer, 0, buffer.length);
                        if (nRead <= 0)
                            break;
                        out.write(buffer, 0, nRead);
                    }
                }
            }

            LOGGER.debug("Adding files to jar completed");
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

    }
}
