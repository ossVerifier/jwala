package com.siemens.cto.aem.service;

import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;

import java.util.List;

public interface JvmInfoService {

    JvmInfo getJvmInfoById(Long id);

    List<JvmInfo> getAllJvmInfo();

    void addJvmInfo(String jvmName, String hostName, GroupInfo groupInfo);

    void updateJvmInfo(Long id, String jvmName, String hostName);

    void deleteJvm(Long id);

}
