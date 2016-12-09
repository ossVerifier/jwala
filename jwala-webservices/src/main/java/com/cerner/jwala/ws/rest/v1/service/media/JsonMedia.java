package com.cerner.jwala.ws.rest.v1.service.media;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO mean to contain de-serialized JSON media data
 * TODO: Try replacing this with {@link com.cerner.jwala.common.domain.model.media.Media}
 *
 * Created by Jedd Cuison on 12/2/2016
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonMedia {

    private String name;
    private String type;
    private String localPath;
    private String remoteDir;
    private String mediaDir;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(final String remoteDir) {
        this.remoteDir = remoteDir;
    }

    public String getMediaDir() {
        return mediaDir;
    }

    public void setMediaDir(final String mediaDir) {
        this.mediaDir = mediaDir;
    }

}
