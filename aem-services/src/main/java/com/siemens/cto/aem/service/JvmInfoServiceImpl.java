package com.siemens.cto.aem.service;

import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.persistence.domain.Jvm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class JvmInfoServiceImpl implements JvmInfoService {

    @Autowired
    private JvmDaoJpa jvmDao;

    @Override
    public Jvm getJvmInfoById(Long id) {
        return jvmDao.findById(id);
    }

    @Override
    public List<Jvm> getAllJvmInfo() {
        return jvmDao.findAll();
    }

    @Override
    @Transactional
    public void addJvmInfo(String jvmName, String hostName) {
        final Jvm jvm = new Jvm();
        jvm.setName(jvmName);
        jvmDao.add(jvm);
    }

    @Override
    @Transactional
    public void updateJvmInfo(Long jvmId, String jvmName, String hostName) {
        final Jvm jvm = new Jvm();
        jvm.setId(jvmId);
        jvm.setName(jvmName);
        jvmDao.update(jvm);
    }

    @Override
    @Transactional
    public void deleteJvm(Long id) {
        jvmDao.remove(jvmDao.findById(id));
    }

    @Override
    public void deleteJvms(List<Jvm> jvms) {
        throw new UnsupportedOperationException();
    }

}
