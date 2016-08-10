package com.cerner.jwala.toc.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface RepositoryService {

    RepositoryFileInformation find(TocPath refPlace, Path partialPath, RepositoryFileInformation... relatedHistory) throws IOException;

    RepositoryFileInformation writeStream(TocPath refPlace, Path partialPath, InputStream transientData, RepositoryFileInformation... relatedHistory) throws IOException;
    
    RepositoryFileInformation deleteIfExisting(TocPath refPlace, Path partialPath, RepositoryFileInformation... relatedHistory) throws IOException;

    RepositoryFileInformation findAll(TocPath refPlace, String pattern, RepositoryFileInformation... relatedHistory) throws IOException;
}
