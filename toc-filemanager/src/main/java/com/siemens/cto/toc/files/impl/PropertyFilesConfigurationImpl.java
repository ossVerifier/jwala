package com.siemens.cto.toc.files.impl;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.toc.files.FilesConfiguration;
import com.siemens.cto.toc.files.TocPath;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Paths specified in TocFiles section as paths.* 
 * Properties defined in {@link TocPath}
 * @author horspe00
 *
 */
public class PropertyFilesConfigurationImpl implements FilesConfiguration {

    private final HashMap<TocPath, Path> paths = new HashMap<>();
    private final FileSystem defaultFs = FileSystems.getDefault();
    
    @Override
    public Path getConfiguredPath(TocPath which) {
        return paths.get(which);
    }

    public PropertyFilesConfigurationImpl(Properties fmProperties) {
        load(fmProperties); 
    }
    
    public void reload() {
        paths.clear();
        load(ApplicationProperties.getProperties());
    }

    public void load(Properties fmProperties) {         
        for(TocPath path : TocPath.values()) {
            paths.put(path, path.getDefaultPath());
        }

        for(Map.Entry<Object, Object> e : fmProperties.entrySet()) {
            if(e.getKey().toString().startsWith("paths.")) {
                for(Map.Entry<TocPath, Path> entry : paths.entrySet()) {
                    if(entry.getKey().getProperty().equalsIgnoreCase(e.getKey().toString())) {
                        entry.setValue(defaultFs.getPath(e.getValue().toString()));
                    }
                }
            }
        }
    }
}
