package com.cerner.jwala.files;

import java.nio.file.Path;

public enum TocPath {

    WEB_ARCHIVE("paths.web-archive"),
    TEMPLATES("paths.templates"), 
    RESOURCE_TEMPLATES("paths.resource-templates")
    ;
    
    
    final String property;
    final Path defaultPath;

    TocPath(final String property) {
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
