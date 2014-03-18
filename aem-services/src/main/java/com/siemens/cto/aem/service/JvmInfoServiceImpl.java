package com.siemens.cto.aem.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.User;
import com.siemens.cto.aem.persistence.dao.GroupDaoJpa;
import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.persistence.domain.JpaGroup;
import com.siemens.cto.aem.persistence.domain.JpaJvm;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotDeletedException;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;

public class JvmInfoServiceImpl implements JvmInfoService {

    private final GroupDaoJpa groupDao;
    private final JvmDaoJpa jvmDao;

    public JvmInfoServiceImpl(GroupDaoJpa groupDao, JvmDaoJpa jvmDao) {
        this.groupDao = groupDao;
        this.jvmDao = jvmDao;
    }

    @Override
    public JvmInfo getJvmInfoById(Long id) {
        final JpaJvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            return new JvmInfo(jvm.getId(),
                               jvm.getName(),
                               jvm.getHostName(),
                               new GroupInfo(jvm.getGroup().getId(), jvm.getGroup().getName()));
        } else {
            throw new RecordNotFoundException(JpaJvm.class, id);
        }
    }

    @Override
    public List<JvmInfo> getAllJvmInfo() {
        final List<JvmInfo> jvmInfoList = new ArrayList<JvmInfo>();
        final List<JpaJvm> jvmList = jvmDao.findAll();
        for (JpaJvm jvm : jvmList) {
            jvmInfoList.add(new JvmInfo(jvm.getId(),
                                        jvm.getName(),
                                        jvm.getHostName(),
                                        new GroupInfo(jvm.getGroup().getId(), jvm.getGroup().getName())));
        }
        return jvmInfoList;
    }

    @Override
    @Transactional
    public void addJvmInfo(String jvmName, String hostName, GroupInfo groupInfo) {

        // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity (no longer, but still necessary in this method until it's more explicitly part of the service interface)
        // TODO: Discuss with the team what module should handle this.
        final User user = new User("testUser", "");
        user.addToThread();

        final JpaJvm jvm = new JpaJvm();
        final String userName = User.getUser().getUserName();
        final Calendar createDate = Calendar.getInstance();

        jvm.setName(jvmName);
        jvm.setHostName(hostName);
        jvm.setCreateBy(userName);
        jvm.setCreateDate(createDate);
        jvm.setUpdateBy(userName);
        jvm.setLastUpdateDate(createDate);

        try {

            JpaGroup group = groupDao.findByName(groupInfo.getName());
            if (group == null) {
                group = new JpaGroup();
                group.setName(groupInfo.getName());
                groupDao.add(group);
                group = groupDao.findByName(groupInfo.getName());
            }

            jvm.setGroup(group);
            jvmDao.add(jvm);

        } catch (EntityExistsException e) {
            throw new RecordNotAddedException(jvm.getClass(), jvm.getName(), e);
        }
    }

    @Override
    @Transactional
    public void updateJvmInfo(Long id, String jvmName, String hostName) {
        // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity (no longer, but still necessary in this method until it's more explicitly part of the service interface)
        // TODO: Discuss with the team what module should handle this.
        final User user = new User("testUser", "");
        user.addToThread();

        final JpaJvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            jvm.setName(jvmName);
            jvm.setHostName(hostName);
            jvm.setLastUpdateDate(Calendar.getInstance());
            jvm.setUpdateBy(user.getUserName());

         } else {
            throw new RecordNotFoundException(JpaJvm.class, id);
        }
    }

    @Override
    @Transactional
    public void updateJvmInfo(Long id, String jvmName, String hostName, String groupName) {
        // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity (no longer, but still necessary in this method until it's more explicitly part of the service interface)
        // TODO: Discuss with the team what module should handle this.
        final User user = new User("testUser", "");
        user.addToThread();

        final JpaJvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            jvm.setName(jvmName);
            jvm.setHostName(hostName);
            jvm.setLastUpdateDate(Calendar.getInstance());
            jvm.setUpdateBy(user.getUserName());

            if (!jvm.getGroup().getName().equalsIgnoreCase(groupName)) {

                JpaGroup group = groupDao.findByName(groupName);
                if (group == null) {
                    group = new JpaGroup();
                    group.setName(groupName);
                    groupDao.add(group);
                    group = groupDao.findByName(groupName);
                }
                jvm.setGroup(group);
            }

        } else {
            throw new RecordNotFoundException(JpaJvm.class, id);
        }
    }

    @Override
    @Transactional
    public void deleteJvm(Long id) {
        final JpaJvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            try {
                // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity
                // TODO: Discuss with the team what module should handle this.
                final User user = new User("testUser", "");
                user.addToThread();

                jvmDao.remove(jvm);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RecordNotDeletedException(JpaJvm.class, id, e);
            }
        } else {
            throw new RecordNotFoundException(JpaJvm.class, id);
        }
    }

    @Override
    public JvmInfo getJvmInfoByName(String name) {
        final JpaJvm jvm = jvmDao.findByName(name);
        if (jvm != null) {
            return new JvmInfo(jvm.getId(),
                    jvm.getName(),
                    jvm.getHostName(),
                    new GroupInfo(jvm.getGroup().getId(), jvm.getGroup().getName()));
        } else {
            throw new RecordNotFoundException(JpaJvm.class, name);
        }
    }

}
