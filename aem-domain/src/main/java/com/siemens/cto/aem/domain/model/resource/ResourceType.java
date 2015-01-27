package com.siemens.cto.aem.domain.model.resource;

import java.util.List;
import java.util.Map;

public class ResourceType {

    String      name;
    String      contentType; 
    List<Map>   properties;

    public ResourceType() { 
        
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<Map> getProperties() {
        return properties;
    }

    public void setProperties(List<Map> properties) {
        this.properties = properties;
    }
}
