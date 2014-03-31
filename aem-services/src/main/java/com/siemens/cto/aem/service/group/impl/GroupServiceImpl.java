package com.siemens.cto.aem.service.group.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;

public class GroupServiceImpl implements GroupService {

    private final GroupDao groupDao;
    private final JvmService jvmService;

    public GroupServiceImpl(final GroupDao theGroupDao,
                            final JvmService theJvmService) {
        groupDao = theGroupDao;
        jvmService = theJvmService;
    }

    @Override
    @Transactional
    public Group createGroup(final CreateGroupCommand aCreateGroupCommand,
                             final User aCreatingUser) {

        if (isValidGroupName(aCreateGroupCommand.getGroupName())) {
            return groupDao.createGroup(createCreateGroupEvent(aCreateGroupCommand,
                                                               aCreatingUser));
        } else {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name is invalid: " + aCreateGroupCommand.getGroupName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final Identifier<Group> aGroupId) {
        return groupDao.getGroup(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups(final PaginationParameter aPaginationParam) {
        return groupDao.getGroups(aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional
    public Group updateGroup(final UpdateGroupCommand anUpdateGroupCommand,
                             final User anUpdatingUser) {
        if (isValidGroupName(anUpdateGroupCommand.getNewName())) {
            return groupDao.updateGroup(createUpdateGroup(anUpdateGroupCommand,
                                                          anUpdatingUser));
        } else {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "New Group Name is invalid: " + anUpdateGroupCommand.getNewName());
        }
    }

    @Override
    @Transactional
    public void removeGroup(final Identifier<Group> aGroupId) {
        jvmService.removeJvmsBelongingTo(aGroupId);
        groupDao.removeGroup(aGroupId);
    }

    @Override
    public Group getGroup(String aGroupName) {
        return groupDao.getGroup(aGroupName);
    }

    protected boolean isValidGroupName(final String aGroupName) {
        return StringUtils.hasText(aGroupName);
    }

    protected Event<CreateGroupCommand> createCreateGroupEvent(final CreateGroupCommand aCreateGroupCommand,
                                                               final User aCreatingUser) {
        return new Event<>(aCreateGroupCommand,
                           AuditEvent.now(aCreatingUser));
    }

    protected Event<UpdateGroupCommand> createUpdateGroup(final UpdateGroupCommand anUpdateGroupCommand,
                                                          final User anUpdatingUser) {
        return new Event<>(anUpdateGroupCommand,
                           AuditEvent.now(anUpdatingUser));
    }
}
