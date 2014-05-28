package com.siemens.cto.toc.files;

import java.nio.file.Path;

public interface Repository {

    void addCategory(String string, Path storageFolder);

}
