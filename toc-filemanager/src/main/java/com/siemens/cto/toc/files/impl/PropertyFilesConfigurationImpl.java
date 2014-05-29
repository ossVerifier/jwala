package com.siemens.cto.toc.files.impl;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.siemens.cto.toc.files.FilesConfiguration;
import com.siemens.cto.toc.files.TocPath;

/**
 * Paths specified in TocFiles section as paths.* 
 * Properties defined in {@link TocPath}
 * @author horspe00
 *
 */
public class PropertyFilesConfigurationImpl implements FilesConfiguration {

    HashMap<TocPath, Path> paths = new HashMap<>();
    FileSystem defaultFs = FileSystems.getDefault();
    
    @Override
    public Path getConfiguredPath(TocPath which) {
        return paths.get(which);
    }
    
    public PropertyFilesConfigurationImpl(Properties fmProperties) {
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
