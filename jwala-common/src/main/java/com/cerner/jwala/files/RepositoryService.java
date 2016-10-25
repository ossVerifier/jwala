package com.cerner.jwala.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface RepositoryService {

    RepositoryFileInformation find(JwalaPath refPlace, Path partialPath, RepositoryFileInformation... relatedHistory) throws IOException;

    RepositoryFileInformation writeStream(JwalaPath refPlace, Path partialPath, InputStream transientData, RepositoryFileInformation... relatedHistory) throws IOException;
    
    RepositoryFileInformation deleteIfExisting(JwalaPath refPlace, Path partialPath, RepositoryFileInformation... relatedHistory) throws IOException;

    RepositoryFileInformation findAll(JwalaPath refPlace, String pattern, RepositoryFileInformation... relatedHistory) throws IOException;
}
