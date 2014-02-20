package com.siemens.cto.aem.ws.rest.vo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "jvmInfoList")
public class JvmInfoVoListWrapper {

    private List<JvmInfoVo> jvmInfoVoList;

    @XmlElement(name = "jvmInfo")
    public List<JvmInfoVo> getJvmInfoList() {
        return jvmInfoVoList;
    }

    public void setJvmInfoList(List<JvmInfoVo> jvmInfoList) {
        this.jvmInfoVoList = jvmInfoList;
    }

}
