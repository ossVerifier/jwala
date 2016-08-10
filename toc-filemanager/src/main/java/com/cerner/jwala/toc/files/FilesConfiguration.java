package com.cerner.jwala.toc.files;

import java.nio.file.Path;

public interface FilesConfiguration {

    Path getConfiguredPath(TocPath webArchive);

    void reload();

}
