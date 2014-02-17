package com.siemens.cto.aem.ws.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class JvmInfo implements Serializable {

    private int jvmId;
    private String name;
    private String host;

    @XmlElement
    public int getJvmId() {
        return jvmId;
    }

    public void setJvmId(int jvmId) {
        this.jvmId = jvmId;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
