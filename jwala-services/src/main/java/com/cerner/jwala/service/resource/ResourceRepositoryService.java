package com.cerner.jwala.service.resource;

import java.io.InputStream;

/**
 * Defines repository related operations on resources
 *
 * Created by Jedd Anthony Cuison on 11/30/2016
 */
public interface ResourceRepositoryService {

    /**
     * uploads a file to the resource repository
     * @param fileName contains info about the upload request
     * @param resource
     * @return absolute location of the file in the repository
     */
    String upload(String fileName, InputStream resource);

    /**
     * removes a file from the resource repository
     * @param filename
     */
    void delete(String filename);

}
