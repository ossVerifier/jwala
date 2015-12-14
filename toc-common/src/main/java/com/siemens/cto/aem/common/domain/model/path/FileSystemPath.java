package com.siemens.cto.aem.common.domain.model.path;

/**
 * This class represents a path that includes the filename (e.g. D:/apache/httpd-2.4.9/conf/httpd.conf).
 * All other file system related operations like getFileName has to go here.
 *
 * Created by z003bpej on 8/28/14.
 */
public class FileSystemPath extends Path {

    public FileSystemPath(final String fileSystemPath) {
        super(fileSystemPath);
    }

}
