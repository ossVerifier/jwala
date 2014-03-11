package com.siemens.cto.aem.service;

import com.siemens.cto.aem.common.User;
import com.siemens.cto.aem.persistence.dao.GroupDaoJpa;
import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.persistence.domain.Group;
import com.siemens.cto.aem.persistence.domain.Jvm;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotDeletedException;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.List;

public class JvmInfoServiceImpl implements JvmInfoService {

    private final GroupDaoJpa groupDao;
    private final JvmDaoJpa jvmDao;

    public JvmInfoServiceImpl(GroupDaoJpa groupDao, JvmDaoJpa jvmDao) {
        this.groupDao = groupDao;
        this.jvmDao = jvmDao;
    }

    @Override
    public JvmInfo getJvmInfoById(Long id) {
        final Jvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            return new JvmInfo(jvm.getId(),
                               jvm.getName(),
                               jvm.getHostName(),
                               new GroupInfo(jvm.getGroup().getId(), jvm.getGroup().getName()));
        } else {
            throw new RecordNotFoundException(Jvm.class, id);
        }
    }

    @Override
    public List<JvmInfo> getAllJvmInfo() {
        final List<JvmInfo> jvmInfoList = new ArrayList<JvmInfo>();
        final List<Jvm> jvmList = jvmDao.findAll();
        for (Jvm jvm : jvmList) {
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
        final Jvm jvm = new Jvm();
        jvm.setName(jvmName);
        jvm.setHostName(hostName);

        // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity
        // TODO: Discuss with the team what module should handle this.
        final User user = new User("testUser", "");
        user.addToThread();

        try {

            Group group = groupDao.findByName(groupInfo.getName());
            if (group == null) {
                group = new Group();
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
        final Jvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            jvm.setName(jvmName);
            jvm.setHostName(hostName);

            // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity
            // TODO: Discuss with the team what module should handle this.
            final User user = new User("testUser", "");
            user.addToThread();

         } else {
            throw new RecordNotFoundException(Jvm.class, id);
        }
    }

    @Override
    @Transactional
    public void updateJvmInfo(Long id, String jvmName, String hostName, String groupName) {
        final Jvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            jvm.setName(jvmName);
            jvm.setHostName(hostName);

            // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity
            // TODO: Discuss with the team what module should handle this.
            final User user = new User("testUser", "");
            user.addToThread();

            if (!jvm.getGroup().getName().equalsIgnoreCase(groupName)) {

                Group group = groupDao.findByName(groupName);
                if (group == null) {
                    group = new Group();
                    group.setName(groupName);
                    groupDao.add(group);
                    group = groupDao.findByName(groupName);
                }
                jvm.setGroup(group);
            }

        } else {
            throw new RecordNotFoundException(Jvm.class, id);
        }
    }

    @Override
    @Transactional
    public void deleteJvm(Long id) {
        final Jvm jvm = jvmDao.findById(id);
        if (jvm != null) {
            try {
                // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity
                // TODO: Discuss with the team what module should handle this.
                final User user = new User("testUser", "");
                user.addToThread();

                jvmDao.remove(jvm);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RecordNotDeletedException(Jvm.class, id, e);
            }
        } else {
            throw new RecordNotFoundException(Jvm.class, id);
        }
    }

    @Override
    public JvmInfo getJvmInfoByName(String name) {
        final Jvm jvm = jvmDao.findByName(name);
        if (jvm != null) {
            return new JvmInfo(jvm.getId(),
                    jvm.getName(),
                    jvm.getHostName(),
                    new GroupInfo(jvm.getGroup().getId(), jvm.getGroup().getName()));
        } else {
            throw new RecordNotFoundException(Jvm.class, name);
        }
    }

}
