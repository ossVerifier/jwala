package com.siemens.cto.toc.files.resources;

import java.io.IOException;
import java.nio.file.Path;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.resource.ResourceType;

public class ResourceTypeDeserializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTypeDeserializer.class);

    private ObjectMapper parser = new ObjectMapper();        

    public ResourceType loadResourceType(Path path) throws IOException { 
        LOGGER.debug("Parsing '" + path + "' as ResourceType");
        ResourceType result = parser.readValue(path.toFile(), ResourceType.class);
        LOGGER.debug("Parsed: " + (( result != null  ) ? result : "(null)"));
        return result;
    }
}
