package com.siemens.cto.toc.files;

import java.nio.file.Path;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A chain of file system operations
 * Intended for chain-of-responsibilities pattern
 * @author horspe00
 */
public class RepositoryAction {

    public enum Type {
        /** No action, for example; deleteIfExisting but not existing */
        NONE,
        /** Verified Exists */
        FOUND, 
        /** Either created or updated */
        STORED,
        /** Deleted from the file system */
        DELETED;
    }

    private Type type;
    private Path path;
    private Long length;
    private RepositoryAction[] inResponseTo;

    public RepositoryAction(Type type, Path path, Long length, RepositoryAction[] inResponseTo) {
        this.path = path;
        this.length = length;
        this.type = type;
        this.inResponseTo = inResponseTo;
    }

    public Type getType() {
        return type;
    }

    public Path getPath() {
        return path;
    }

    public Long getLength() {
        return length;
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

    public static RepositoryAction found(Path resolvedPath, RepositoryAction... inResponseTo) {
        return new RepositoryAction(Type.FOUND, resolvedPath, null, inResponseTo);
    }

    public static RepositoryAction none(RepositoryAction... inResponseTo) {
        return new RepositoryAction(Type.NONE, null, null, inResponseTo);
    }

    @Override
    public String toString() {
        String msg = type.toString() + " " + path + ((length != null) ? ";" + length : "");
        String csep=" -> ";

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
                .append(this.path, rhs.path)
                .append(this.length, rhs.length)
                .append(this.inResponseTo, rhs.inResponseTo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(type)
                .append(path)
                .append(length)
                .append(inResponseTo)
                .toHashCode();
    }
}
