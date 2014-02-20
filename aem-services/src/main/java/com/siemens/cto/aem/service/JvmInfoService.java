package com.siemens.cto.aem.service;

import com.siemens.cto.aem.persistence.domain.Jvm;

import java.util.List;

public interface JvmInfoService {

    Jvm getJvmInfoById(Long id);

    List<Jvm> getAllJvmInfo();

    void addJvmInfo(String jvmName, String hostName);

    void updateJvmInfo(Long jvmId, String jvmName, String hostName);

    void deleteJvm(Long id);

    void deleteJvms(List<Jvm> jvm);

}
