package com.siemens.cto.toc.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface Repository {

    RepositoryAction writeStream(TocPath refPlace, Path filename, InputStream transientData, RepositoryAction... inResponseTo) throws IOException;
    
    RepositoryAction deleteIfExisting(TocPath refPlace, Path filename, RepositoryAction... inResponseTo) throws IOException;
}
