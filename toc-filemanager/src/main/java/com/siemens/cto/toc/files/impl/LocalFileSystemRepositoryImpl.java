package com.siemens.cto.toc.files.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.toc.files.FilesConfiguration;
import com.siemens.cto.toc.files.Repository;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.RepositoryAction.Type;
import com.siemens.cto.toc.files.TocPath;

public class LocalFileSystemRepositoryImpl implements Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileSystemRepositoryImpl.class);

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
        return RepositoryAction.none(inResponseTo);
    }

    @Override
    public RepositoryAction findAll(TocPath refPlace, String filter, RepositoryAction... inResponseTo) throws IOException {
        Path place = filesConfiguration.getConfiguredPath(refPlace);
        List<Path> results = new LinkedList<Path>();

        try(DirectoryStream<Path> directory = Files.newDirectoryStream(place, filter)) {
            
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Inspecting path '"+place.toAbsolutePath().toString()+"' for children matching '"+filter+"'"); 
            }
            
            for(Path p : directory) { 

                LOGGER.debug("Found file: '"+p.toAbsolutePath().toString()+"'"); 
                results.add(p); 
            }
                
            if(results.size() > 0) {
                return RepositoryAction.found(results, inResponseTo);
            }
        }

        return RepositoryAction.none(inResponseTo);
    }
}
