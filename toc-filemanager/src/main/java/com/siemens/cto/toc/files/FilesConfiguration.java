package com.siemens.cto.toc.files;

import java.nio.file.Path;

public interface FilesConfiguration {

    Path getConfiguredPath(TocPath webArchive);

}
