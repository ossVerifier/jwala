package com.siemens.cto.toc.files;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public enum TocPath {

    WEB_ARCHIVE("paths.web-archive"), 
    TEMPLATES("paths.templates"), 
    RESOURCE_TYPES("paths.resource-types")
    ;
    
    
    final String property;
    final Path defaultPath;
    TocPath(final String property, final String defaultPath) {
        this.property = property;
        this.defaultPath = FileSystems.getDefault().getPath(defaultPath).toAbsolutePath();
    }
    TocPath(final String property) {
        this.property = property;
        this.defaultPath = null;
    }
    
    public String getProperty() { return property; }
    public Path getDefaultPath() { return defaultPath; } 
    
}
