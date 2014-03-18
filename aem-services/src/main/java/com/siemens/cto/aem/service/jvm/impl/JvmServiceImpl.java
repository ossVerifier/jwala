package com.siemens.cto.aem.service.jvm.impl;

import java.util.List;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.jvm.JvmDao;
import com.siemens.cto.aem.service.jvm.JvmService;

public class JvmServiceImpl implements JvmService {

    private JvmDao dao;

    public JvmServiceImpl(final JvmDao theDao) {
        dao = theDao;
    }

    @Override
    public Jvm createJvm(final CreateJvmCommand aCreateJvmCommand,
                         final User aCreatingUser) {

        aCreateJvmCommand.validateCommand();

        final Event<CreateJvmCommand> event = new Event<>(aCreateJvmCommand,
                                                          AuditEvent.now(aCreatingUser));

        return  dao.createJvm(event);
    }

    @Override
    public Jvm getJvm(final Identifier<Jvm> aJvmId) {

        return dao.getJvm(aJvmId);
    }

    @Override
    public List<Jvm> getJvms(final PaginationParameter aPaginationParam) {

        return dao.getJvms(aPaginationParam);
    }

    @Override
    public List<Jvm> findJvms(final String aJvmNameFragment,
                              final PaginationParameter aPaginationParam) {

        return dao.findJvms(aJvmNameFragment,
                            aPaginationParam);
    }

    @Override
    public List<Jvm> findJvms(final Identifier<Group> aJvmId,
                              final PaginationParameter aPaginationParam) {

        return dao.findJvmsBelongingTo(aJvmId,
                                       aPaginationParam);

    }

    @Override
    public Jvm updateJvm(final UpdateJvmCommand anUpdateJvmCommand,
                         final User anUpdatingUser) {

        anUpdateJvmCommand.validateCommand();

        final Event<UpdateJvmCommand> event = new Event<>(anUpdateJvmCommand,
                                                          AuditEvent.now(anUpdatingUser));

        return dao.updateJvm(event);
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {

        dao.removeJvm(aJvmId);
    }

    @Override
    public void removeJvmsBelongingTo(final Identifier<Group> aGroupId) {
        dao.removeJvmsBelongingTo(aGroupId);
    }
}
