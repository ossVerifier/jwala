package com.siemens.cto.toc.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface Repository {

    /**
     * Write a file
     * @return number of bytes written
     * @throws IOException
     */
    int writeStream(TocPath refPlace, Path filename, InputStream transientData) throws IOException;

}
