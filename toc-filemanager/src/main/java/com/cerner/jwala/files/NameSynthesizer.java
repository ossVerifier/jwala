package com.cerner.jwala.files;

import java.nio.file.Path;

public interface NameSynthesizer {

    Path unique(Path original);

}
