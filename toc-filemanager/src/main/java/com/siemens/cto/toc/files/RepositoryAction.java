package com.siemens.cto.toc.files;

import java.nio.file.Path;

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
                if(action != null) {
                    msg = msg + csep + action.toString();
                    csep = ", ";
                }
            }
        }
        
        return msg;
    }

}
