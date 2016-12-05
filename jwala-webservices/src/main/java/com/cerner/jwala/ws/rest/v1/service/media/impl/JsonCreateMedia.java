package com.cerner.jwala.ws.rest.v1.service.media.impl;

/**
 * Created by RS045609 on 12/2/2016.
 */
public class JsonCreateMedia {
    private final String mediaName;
    private final String path;
    private final String type;
    private final String remoteHostPath;

    public JsonCreateMedia(String mediaName, String path, String type, String remoteHostPath) {
        this.mediaName = mediaName;
        this.path = path;
        this.type = type;
        this.remoteHostPath = remoteHostPath;
    }


}
