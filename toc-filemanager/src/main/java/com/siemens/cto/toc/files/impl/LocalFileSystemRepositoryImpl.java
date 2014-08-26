package com.siemens.cto.toc.files.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.toc.files.FilesConfiguration;
import com.siemens.cto.toc.files.Repository;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.RepositoryAction.Type;
import com.siemens.cto.toc.files.TocPath;

public class LocalFileSystemRepositoryImpl implements Repository {

    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Override
    public RepositoryAction writeStream(TocPath refPlace, Path file, InputStream transientData, RepositoryAction... inResponseTo) throws IOException {
        Path place = filesConfiguration.getConfiguredPath(refPlace);
        Path resolvedPath = place.resolve(file).toAbsolutePath().normalize();
        long copied = 0;
        try(
                FileChannel out = FileChannel.open(resolvedPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
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
        return RepositoryAction.stored(resolvedPath, (Long)copied, inResponseTo);
        
    }

    @Override
    public RepositoryAction deleteIfExisting(TocPath refPlace, Path file, RepositoryAction... inResponseTo) throws IOException {
        RepositoryAction res1 = find(refPlace, file, inResponseTo);
        if(res1.getType() == Type.FOUND) {
            Files.delete(res1.getPath());
            return RepositoryAction.deleted(res1.getPath(), res1);
        } else {
            return res1;
        }
    }

    @Override
    public RepositoryAction find(TocPath refPlace, Path filename, RepositoryAction... inResponseTo) throws IOException {
        Path place = filesConfiguration.getConfiguredPath(refPlace);
        Path resolvedPath = place.resolve(filename);
        if(Files.exists(resolvedPath)) {
            return RepositoryAction.found(resolvedPath, inResponseTo);
        }
        return RepositoryAction.none();
    }

}
