package com.cerner.jwala.files;

import java.nio.file.Path;

public interface FilesConfiguration {

    Path getConfiguredPath(JwalaPath webArchive);

    void reload();

}
