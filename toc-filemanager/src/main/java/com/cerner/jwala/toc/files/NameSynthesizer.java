package com.cerner.jwala.toc.files;

import java.nio.file.Path;

public interface NameSynthesizer {

    Path unique(Path original);

}
