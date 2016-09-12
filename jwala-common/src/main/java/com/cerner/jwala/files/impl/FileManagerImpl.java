package com.cerner.jwala.files.impl;

import com.cerner.jwala.files.*;
import com.cerner.jwala.files.RepositoryFileInformation.Type;
import com.cerner.jwala.files.resources.ResourceTypeDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileManagerImpl implements FileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManagerImpl.class);
    public static final String JSON = "*.json";
    public static final String TEMPLATE_TPL = "Template.tpl";

    @Autowired
    private RepositoryService fileSystemStorage;

    @Autowired
    private ResourceTypeDeserializer resourceTypeDeserializer;

    @Override
    public String getAbsoluteLocation(TocFile templateName) throws IOException {
        return fileSystemStorage.find(TocPath.TEMPLATES, FileSystems.getDefault().getPath(templateName.getFileName())).getFoundPath().toString();
    }

    @Override
    public String getResourceTypeTemplate(final String resourceTypeName) {
        if (resourceTypeName != null) {
            try {
                return read(this.getResourceTypeTemplateByStream(resourceTypeName));
            } catch (IOException ioe) {
                LOGGER.error("Failed to read {}", resourceTypeName, ioe);
            }
        }
        return null;
    }

    @Override
    public InputStream getResourceTypeTemplateByStream(String resourceTypeName) {
        try {
            // TODO: Figure out if this the best way to derive at the template name (by getting the resource type name and removing the spaces and assuming that the they would be the same as that of the file name).
            String resourceTypeNameNoWS = StringUtils.replace(resourceTypeName, " ", "");
            RepositoryFileInformation fileInformation = fileSystemStorage.find(TocPath.RESOURCE_TEMPLATES, Paths.get(resourceTypeNameNoWS + TEMPLATE_TPL));
            if (fileInformation.getType().equals(Type.FOUND)) {
                return this.readFile(fileInformation.getPath());
            }
        } catch (IOException ioe) {
            LOGGER.error("Failed to read {}", resourceTypeName, ioe);
        }
        return null;
    }

    @Override
    public String getMasterTemplate(String masterTemplateName) {
        try {
            return read(this.getMasterTemplateByStream(masterTemplateName));
        } catch (IOException ioe) {
            LOGGER.error("Failed to read {} " + masterTemplateName, ioe);
        }
        return null;
    }

    @Override
    public InputStream getMasterTemplateByStream(String masterTemplateName) {
        try {
            RepositoryFileInformation fileInformation = fileSystemStorage.find(TocPath.TEMPLATES, Paths.get(masterTemplateName + ".tpl"));
            if (fileInformation.getType().equals(Type.FOUND)) {
                return readFile(fileInformation.getPath());
            }
        } catch (IOException ioe) {
            LOGGER.error("Failed to read {}", masterTemplateName, ioe);
        }
        return null;
    }

    private InputStream readFile(Path path) throws IOException {
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    private String read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = reader.readLine();
        }
        return sb.toString();
    }

    /**
     *
     * @param zipFile
     * @param destDir
     * @throws IOException
     */
    @Override
    public void unZipFile(File zipFile, File destDir) throws IOException {
        LOGGER.debug("zipFile: " + zipFile.getAbsolutePath());
        LOGGER.debug("destDir: " + destDir.getAbsolutePath());
        if(!destDir.exists()) {
            destDir.mkdir();
        }
        //TODO: can be optimized
        JarFile zip = new JarFile(zipFile);
        Enumeration enumEntries = zip.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry file = (JarEntry) enumEntries.nextElement();
            File f = new File(destDir + File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            InputStream is = zip.getInputStream(file); // get the input stream
            FileOutputStream fos = new FileOutputStream(f);
            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
    }

}
