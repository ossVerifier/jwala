package com.siemens.cto.aem.service;

import java.util.List;

public interface JvmInfoService {

    JvmInfo getJvmInfoById(Long id);

    List<JvmInfo> getAllJvmInfo();

    void addJvmInfo(String jvmName, String hostName);

    void updateJvmInfo(Long id, String jvmName, String hostName);

    void deleteJvm(Long id);

}
