package com.siemens.cto.toc.files.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.toc.files.FilesConfiguration;
import com.siemens.cto.toc.files.Repository;
import com.siemens.cto.toc.files.TocPath;

public class LocalFileSystemRepositoryImpl implements Repository {

    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Override
    public int writeStream(TocPath refPlace, Path file, InputStream transientData) throws IOException {
        Path place = filesConfiguration.getConfiguredPath(refPlace);

        int copied = 0;
        try(
                FileChannel out = FileChannel.open(place.resolve(file), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                ReadableByteChannel in = Channels.newChannel(transientData);   
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
              buffer.flip();
              int segment = out.write(buffer);
              if(segment != len) {
                  throw new IOException("Write failed");
              }
              copied += len;
              buffer.clear();
            }
        }        
        return copied;
        
    }

}
