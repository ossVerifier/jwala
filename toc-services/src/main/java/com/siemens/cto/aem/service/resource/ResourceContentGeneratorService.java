package com.siemens.cto.aem.service.resource;

/**
 * Contract for a service that generates resource content
 *
 * Created by JC043760 on 7/26/2016
 */
public interface ResourceContentGeneratorService {

    /**
     * Generate a resource content from a template by merging data
     * @param template the template
     * @param entity an entity that contains data to map to the template e.g. JVM, WebServer etc...
     * @return the content (template + data)
     */
    <T> String generateContent(final String template, T entity);

}
