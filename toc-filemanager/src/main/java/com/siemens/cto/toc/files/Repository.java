package com.siemens.cto.toc.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface Repository {

    RepositoryAction find(TocPath refPlace, Path filename, RepositoryAction... inResponseTo) throws IOException;

    RepositoryAction writeStream(TocPath refPlace, Path filename, InputStream transientData, RepositoryAction... inResponseTo) throws IOException;
    
    RepositoryAction deleteIfExisting(TocPath refPlace, Path filename, RepositoryAction... inResponseTo) throws IOException;

    RepositoryAction findAll(TocPath refPlace, String filter, RepositoryAction... inResponseTo) throws IOException;
}
