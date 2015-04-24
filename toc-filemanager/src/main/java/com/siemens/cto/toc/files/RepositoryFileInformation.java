package com.siemens.cto.toc.files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

/**
 * A chain of file system operations
 * Intended for chain-of-responsibilities pattern
 * @author horspe00
 */
public class RepositoryFileInformation implements Iterable<Path> {

    private Logger logger = Logger.getLogger(RepositoryFileInformation.class);

    public enum Type {
        /** No action, for example; deleteIfExisting but not existing */
        NONE,
        /** Verified one or more results exist */
        FOUND, 
        /** Either created or updated */
        STORED,
        /** Deleted from the file system */
        DELETED;
    }

    private class Entry {
        public Path path;
        public Long length;
        
        public Entry(Path path, Long length) {
            this.path = path;
            this.length = length;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Entry rhs = (Entry) obj;
            return new EqualsBuilder()
                    .append(this.path, rhs.path)
                    .append(this.length, rhs.length)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(path)
                    .append(length)
                    .toHashCode();
        }
    }

    protected class EntryIterator implements Iterator<Path> {
        public int index = 0;

        @Override
        public boolean hasNext() {
            return index < RepositoryFileInformation.this.paths.length;
        }

        @Override
        public Path next() {
            if(index < RepositoryFileInformation.this.paths.length) {
                return RepositoryFileInformation.this.paths[index++].path;
            } 
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        } 
    }

    private final Type type;
    private final Entry[] paths;
    private final RepositoryFileInformation[] relatedHistory;

    public RepositoryFileInformation(final Type type, final Path path, final Long length, final RepositoryFileInformation[] relatedHistory) {
        this.paths = new Entry[] { new Entry(path, length) };
        this.type = type;
        this.relatedHistory = relatedHistory != null ? Arrays.copyOf(relatedHistory, relatedHistory.length) : null;
    }

    public RepositoryFileInformation(final Type type, final List<Path> theFoundPaths, final RepositoryFileInformation[] relatedHistory) {
        this.type = type;
        this.paths = new Entry[theFoundPaths.size()];
        this.relatedHistory = relatedHistory != null ? Arrays.copyOf(relatedHistory, relatedHistory.length) : null;
        int idx = -1;
        for(Path path : theFoundPaths) { 
            this.paths[++idx] = new Entry(path, path.toFile().length());
        }
    }

    public Type getType() {
        return type;
    }

    public Path getFoundPath() throws FileNotFoundException {
        if(type == Type.FOUND) { 
            return paths[0].path;
        } else {
            throw new FileNotFoundException(paths[0].path.toString());
        }
    }

    public Path getPath() {
        return paths[0].path;
    }

    public Long getLength() {
        return paths[0].length;
    }

    public RepositoryFileInformation[] getCauses() {
        return relatedHistory;
    }

    public static RepositoryFileInformation deleted(Path resolvedPath, RepositoryFileInformation... inResponseTo) {
        return new RepositoryFileInformation(Type.DELETED, resolvedPath, null, inResponseTo);
    }

    public static RepositoryFileInformation stored(Path resolvedPath, Long copied, RepositoryFileInformation... inResponseTo) {
        return new RepositoryFileInformation(Type.STORED, resolvedPath, copied, inResponseTo);
    }

    public static RepositoryFileInformation found(List<Path> paths, RepositoryFileInformation... inResponseTo) {
        return new RepositoryFileInformation(Type.FOUND, paths, inResponseTo);
    }
    public static RepositoryFileInformation found(Path resolvedPath, RepositoryFileInformation... inResponseTo) {
        return new RepositoryFileInformation(Type.FOUND, resolvedPath, null, inResponseTo);
    }

    public static RepositoryFileInformation none(RepositoryFileInformation... inResponseTo) {
        return new RepositoryFileInformation(Type.NONE, null, null, inResponseTo);
    }

    @Override
    public String toString() {
        String csep="";
        String msg= type.toString() + "[ ";
        for(Entry e : paths) {             
             msg = msg + csep + e.path + ((e.length != null) ? ";" + e.length : "");
             csep = ", ";
        }
        msg = msg + " ]";
        csep=" -> ";

        if(relatedHistory != null) {
            for(RepositoryFileInformation action : relatedHistory) {
                if(action != null && action.getType() != Type.NONE) {
                    msg = msg + csep + action.toString();
                    csep = ", ";
                }
            }
        }

        return msg;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        RepositoryFileInformation rhs = (RepositoryFileInformation) obj;
        return new EqualsBuilder()
                .append(this.type, rhs.type)
                .append(this.paths, rhs.paths)
                .append(this.relatedHistory, rhs.relatedHistory)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(type)
                .append(paths)
                .append(relatedHistory)
                .toHashCode();
    }

    @Override
    public Iterator<Path> iterator() {        
        return new EntryIterator();
    }

}
