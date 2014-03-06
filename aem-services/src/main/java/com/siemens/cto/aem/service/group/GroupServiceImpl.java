package com.siemens.cto.aem.service.group;

import java.sql.Date;
import java.util.List;

import org.springframework.util.StringUtils;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.audit.AuditDateTime;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.audit.AuditUser;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.CreateGroup;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;

public class GroupServiceImpl implements GroupService {

    private final GroupDao groupDao;

    public GroupServiceImpl(final GroupDao theGroupDao) {
        groupDao = theGroupDao;
    }

    @Override
    public Group createGroup(final String aNewGroupName,
                             final User aCreatingUser) {

        if (isValidGroupName(aNewGroupName)) {
            return groupDao.createGroup(createCreateGroup(aNewGroupName,
                                                          aCreatingUser));
        } else {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name is invalid: " + aNewGroupName);
        }
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId) {
        return groupDao.getGroup(aGroupId);
    }

    @Override
    public List<Group> getGroups(final PaginationParameter aPaginationParam) {
        return groupDao.getGroups(aPaginationParam);
    }

    @Override
    public List<Group> findGroups(final String aGroupNameFragment,
                                  final PaginationParameter aPaginationParam) {
        if (isValidGroupName(aGroupNameFragment)) {
            return groupDao.findGroups(aGroupNameFragment,
                                       aPaginationParam);
        } else {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name fragment is invalid: " + aGroupNameFragment);
        }
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) {
        groupDao.removeGroup(aGroupId);
    }

    protected boolean isValidGroupName(final String aGroupName) {
        return StringUtils.hasText(aGroupName);
    }

    protected CreateGroup createCreateGroup(final String aNewGroupName,
                                            final User aCreatingUser) {
        return new CreateGroup(aNewGroupName,
                               new AuditEvent(new AuditUser(aCreatingUser.getId()),
                                              new AuditDateTime(new Date(System.currentTimeMillis()))));
    }
}
