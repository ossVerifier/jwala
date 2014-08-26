package com.siemens.cto.toc.files.impl;

import java.nio.file.Path;
import java.util.UUID;

import com.siemens.cto.toc.files.NameSynthesizer;

public class DefaultNameSynthesizer implements NameSynthesizer {

    @Override
    public Path unique(Path path) {
        String fn = path.getFileName().toString();
        int idx = fn.lastIndexOf('.');
        String prefix, suffix;
        if(idx == -1) { 
            prefix = fn;
            suffix = "";
        } else {
            prefix = fn.substring(0, idx);
            suffix = fn.substring(idx);
        }
        return path.getFileSystem().getPath(prefix + "-" + UUID.randomUUID().toString() + suffix);
    }

}
