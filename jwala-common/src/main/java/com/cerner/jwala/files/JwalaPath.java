package com.cerner.jwala.files;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;

import java.nio.file.Path;

public enum JwalaPath {

    WEB_ARCHIVE(ApplicationProperties.get(PropertyKeys.PATHS_WEB_ARCHIVE)),
    TEMPLATES("paths.templates"), 
    RESOURCE_TEMPLATES(ApplicationProperties.get(PropertyKeys.PATHS_RESOURCE_TEMPLATES))
    ;
    
    
    final String property;
    final Path defaultPath;

    JwalaPath(final String property) {
        this.property = property;
        this.defaultPath = null;
    }
    
    public String getProperty() {
        return property;
    }

    public Path getDefaultPath() {
        return defaultPath;
    }

}
