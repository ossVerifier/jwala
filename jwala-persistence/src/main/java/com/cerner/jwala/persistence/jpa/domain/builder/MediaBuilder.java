package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.persistence.jpa.domain.Media;
import com.cerner.jwala.persistence.jpa.type.MediaType;

public class MediaBuilder {

    private String name;
    private MediaType type;
    private String localPath;
    private String remotePath;

    public MediaBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public MediaBuilder setType(MediaType type) {
        this.type = type;
        return this;
    }

    public MediaBuilder setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

    public MediaBuilder setRemotePath(String remotePath) {
        this.remotePath = remotePath;
        return this;
    }

    public Media build() {
        return new Media(name, type, localPath, remotePath);
    }

}