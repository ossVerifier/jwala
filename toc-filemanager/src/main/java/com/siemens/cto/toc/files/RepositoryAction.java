package com.siemens.cto.toc.files;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * A chain of file system operations
 * Intended for chain-of-responsibilities pattern
 * @author horspe00
 */
public class RepositoryAction {
    
    public enum Type { 
        /** No action, for example; deleteIfExisting but not existing */
        NONE,
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RepositoryAction other = (RepositoryAction) obj;
        if (length == null) {
            if (other.length != null) {
                return false;
            }
        } else if (!length.equals(other.length)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

}
