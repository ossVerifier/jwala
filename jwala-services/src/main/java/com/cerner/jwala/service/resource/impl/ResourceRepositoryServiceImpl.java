package com.cerner.jwala.service.resource.impl;

import com.cerner.jwala.service.resource.ResourceRepositoryService;
import com.cerner.jwala.service.resource.ResourceRepositoryServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Implements {@link ResourceRepositoryServiceImpl}
 *
 * Created by Jedd Anthony Cuison on 11/30/2016
 */
@Service
public class ResourceRepositoryServiceImpl implements ResourceRepositoryService {

    public static final int BYTE_ARRAY_SIZE = 1024;
    private final Path repositoryPath;

    public ResourceRepositoryServiceImpl(@Value("${paths.web-archive}") final String repositoryPath) {
        this.repositoryPath = Paths.get(repositoryPath);
    }

    @Override
    public String upload(final String filename, final InputStream resource) {
        try {
            final String absoluteFilename = repositoryPath.toAbsolutePath().normalize().toString() + "/" +
                                                getResourceNameUniqueName(filename);
            final FileOutputStream out = new FileOutputStream(absoluteFilename);
            final byte [] bytes = new byte[BYTE_ARRAY_SIZE];
            int byteCount;
            while ((byteCount = resource.read(bytes)) != -1) {
                out.write(bytes, 0, byteCount);
            }
            return absoluteFilename;
        } catch (final IOException e) {
            throw new ResourceRepositoryServiceException("Resource upload failed!", e);
        }
    }

    @Override
    public void delete(final String filename) {
        final File file = new File(filename);
        if (file.delete()) {
            throw new ResourceRepositoryServiceException("Failed to delete " + filename + "!");
        }
    }

    /**
     * Generate a unique file name for a resource e.g. hct.war -> hct-e60b9a77-9ac3-443a-85ee-5cc001b62d80.war
     * @param name the name of the resource
     * @return the resource name with a UUID for uniqueness
     */
    private String getResourceNameUniqueName(final String name) {
        int idx = name.lastIndexOf('.');
        String prefix, suffix;
        if(idx == -1) {
            prefix = name;
            suffix = "";
        } else {
            prefix = name.substring(0, idx);
            suffix = name.substring(idx);
        }
        return prefix + "-" + UUID.randomUUID().toString() + suffix;
    }

}
