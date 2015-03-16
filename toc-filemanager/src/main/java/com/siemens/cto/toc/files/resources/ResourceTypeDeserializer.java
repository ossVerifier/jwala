package com.siemens.cto.toc.files.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.resource.ResourceType;

public class ResourceTypeDeserializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTypeDeserializer.class);

    private ObjectMapper parser = new ObjectMapper();        

    public ResourceType loadResourceType(Path path) { 
        LOGGER.debug("Parsing '" + path + "' as ResourceType");
        try {
            ResourceType result = parser.readValue(path.toFile(), ResourceType.class);
            LOGGER.debug("Parsed: " + (( result != null  ) ? result : "(null)"));
            result.setValid(true);
            return result;
        } catch(JsonParseException | JsonMappingException e) {
            return createInvalidResourceType( path, e );
        } catch (IOException e) {
            return createInvalidResourceType( path, e );            
        }
    }
    
    private ResourceType createInvalidResourceType(Path path, Throwable exception) {
        
        ResourceType r = new ResourceType();
        
        r.setValid(false);

        try {
            r.setName(parseRootNameFromFile(path));
        } catch (Exception e) {
            r.setName(path.toString());
            LOGGER.debug("Constructing invalid response with file naming exception", e);
        }
        
        Map<String, String> exceptionProp = r.addException(exception);
        
        try(BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset())) {
            exceptionProp.put("content", exception.toString());
            LOGGER.debug("exception property set to exception caught.", exception);
        } catch (IOException e) {
            exceptionProp.put("content", "Could not read file.");
            LOGGER.debug("exception property set to file not found.", e);
        }
        
        return r;
    }
    
    public static String parseRootNameFromFile(Path path) {
        String prefix = path.getFileName().toString().split(Pattern.quote("."))[0];
        String name = prefix.substring(0, prefix.length()-"Properties".length());
        return name;
    }
}
