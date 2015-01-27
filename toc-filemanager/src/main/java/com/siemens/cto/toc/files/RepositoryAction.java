package com.siemens.cto.toc.files;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A chain of file system operations
 * Intended for chain-of-responsibilities pattern
 * @author horspe00
 */
public class RepositoryAction implements Iterable<Path> {

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
            return index < RepositoryAction.this.paths.length;
        }

        @Override
        public Path next() {
            if(index < RepositoryAction.this.paths.length) {
                return RepositoryAction.this.paths[index++].path;
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
    private final RepositoryAction[] inResponseTo;

    public RepositoryAction(final Type type, final Path path, final Long length, final RepositoryAction[] inResponseTo) {
        this.paths = new Entry[] { new Entry(path, length) };
        this.type = type;
        this.inResponseTo = inResponseTo;
    }

    public RepositoryAction(final Type type, final List<Path> theFoundPaths, final RepositoryAction[] inResponseTo) {
        this.type = type;
        this.paths = new Entry[theFoundPaths.size()];
        this.inResponseTo = inResponseTo;
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

    public RepositoryAction[] getCauses() {
        return inResponseTo;
    }

    public static RepositoryAction deleted(Path resolvedPath, RepositoryAction... inResponseTo) {
        return new RepositoryAction(Type.DELETED, resolvedPath, null, inResponseTo);
    }

    public static RepositoryAction stored(Path resolvedPath, Long copied, RepositoryAction... inResponseTo) {
        return new RepositoryAction(Type.STORED, resolvedPath, copied, inResponseTo);
    }

    public static RepositoryAction found(List<Path> paths, RepositoryAction... inResponseTo) {
        return new RepositoryAction(Type.FOUND, paths, inResponseTo);
    }
    public static RepositoryAction found(Path resolvedPath, RepositoryAction... inResponseTo) {
        return new RepositoryAction(Type.FOUND, resolvedPath, null, inResponseTo);
    }

    public static RepositoryAction none(RepositoryAction... inResponseTo) {
        return new RepositoryAction(Type.NONE, null, null, inResponseTo);
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

        if(inResponseTo != null) {
            for(RepositoryAction action : inResponseTo) {
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
        RepositoryAction rhs = (RepositoryAction) obj;
        return new EqualsBuilder()
                .append(this.type, rhs.type)
                .append(this.paths, rhs.paths)
                .append(this.inResponseTo, rhs.inResponseTo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(type)
                .append(paths)
                .append(inResponseTo)
                .toHashCode();
    }

    @Override
    public Iterator<Path> iterator() {        
        return new EntryIterator();
    }

}
