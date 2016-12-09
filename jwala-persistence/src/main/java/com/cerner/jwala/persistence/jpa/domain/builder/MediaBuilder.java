package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.persistence.jpa.domain.Media;
import com.cerner.jwala.persistence.jpa.type.MediaType;

import java.nio.file.Paths;

public class MediaBuilder {

    private String name;
    private MediaType type;
    private String localPath;
    private String remoteDir;
    private String mediaDir;

    public MediaBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public MediaBuilder setType(final MediaType type) {
        this.type = type;
        return this;
    }

    public MediaBuilder setLocalPath(final String localPath) {
        this.localPath = localPath;
        return this;
    }

    public MediaBuilder setRemoteDir(final String remoteDir) {
        this.remoteDir = remoteDir;
        return this;
    }

    public MediaBuilder setMediaDir(String mediaDir) {
        this.mediaDir = mediaDir;
        return this;
    }

    public Media build() {
        final Media media = new Media();
        media.setName(name);
        media.setType(type);
        media.setLocalPath(Paths.get(localPath));
        media.setRemoteDir(Paths.get(remoteDir));
        media.setMediaDir(Paths.get(mediaDir));
        return media;
    }

}