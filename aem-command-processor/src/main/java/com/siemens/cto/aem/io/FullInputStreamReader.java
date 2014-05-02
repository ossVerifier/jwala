package com.siemens.cto.aem.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FullInputStreamReader {

    private final InputStream inputStream;
    private final int bufferSize;

    public FullInputStreamReader(final InputStream theInputStream) {
        this(theInputStream,
             4096);
    }

    public FullInputStreamReader(final InputStream theInputStream,
                                 final int theBufferSize) {
        inputStream = theInputStream;
        bufferSize = theBufferSize;
    }

    public String readString() throws IOException {
        return readString(StandardCharsets.UTF_8);
    }

    public String readString(final Charset aCharacterSet) throws IOException {
        return readStream().toString(aCharacterSet.name());
    }

    public byte[] readBytes() throws IOException {
        return readStream().toByteArray();
    }

    protected ByteArrayOutputStream readStream() throws IOException {
        final ByteArrayOutputStream bytesRead = new ByteArrayOutputStream();
        final byte[] buffer = new byte[bufferSize];
        int numberRead;
        while ((numberRead = inputStream.read(buffer)) != -1) {
            bytesRead.write(buffer,
                            0,
                            numberRead);
        }

        return bytesRead;
    }
}
