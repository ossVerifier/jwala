package com.siemens.cto.aem.io;

import java.io.InputStream;
import java.util.concurrent.Callable;

public class FullInputStreamReaderTask implements Callable<String> {

    private final FullInputStreamReader reader;

    public FullInputStreamReaderTask(final InputStream theInputStream) {
        this(new FullInputStreamReader(theInputStream));
    }

    public FullInputStreamReaderTask(final FullInputStreamReader theReader) {
        reader = theReader;
    }

    @Override
    public String call() throws Exception {
        return reader.readString();
    }
}
