package com.siemens.cto.aem.ws.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "jvmInfoList")
public class JvmInfoListWrapper {

    private List<JvmInfo> jvmInfoList;

    @XmlElement(name = "jvmInfo")
    public List<JvmInfo> getJvmInfoList() {
        return jvmInfoList;
    }

    public void setJvmInfoList(List<JvmInfo> jvmInfoList) {
        this.jvmInfoList = jvmInfoList;
    }

}
