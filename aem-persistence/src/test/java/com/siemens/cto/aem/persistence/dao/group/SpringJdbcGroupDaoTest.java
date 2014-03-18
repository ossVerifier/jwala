package com.siemens.cto.aem.persistence.dao.group;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.group.impl.SpringJdbcGroupDaoImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringJdbcGroupDaoTest {

    private NamedParameterJdbcTemplate template;
    private SpringJdbcGroupDaoImpl dao;

    @Before
    public void setup() {
        template = mock(NamedParameterJdbcTemplate.class);
        dao = new SpringJdbcGroupDaoImpl(template);
    }

    @Test(expected = RuntimeException.class)
    public void testFailedInsert() {

        when(template.update(Matchers.anyString(),
                             Matchers.<SqlParameterSource>anyObject(),
                             Matchers.<KeyHolder>anyObject())).thenReturn(0);

        createGroup();
    }

    @Test(expected = BadRequestException.class)
    public void testDuplicateGroupName() {

        when(template.update(Matchers.anyString(),
                             Matchers.<SqlParameterSource>anyObject(),
                             Matchers.<KeyHolder>anyObject())).thenThrow(DuplicateKeyException.class);

        createGroup();
    }

    @Test(expected = NotFoundException.class)
    public void testNotFoundUpdate() {

        when(template.update(Matchers.anyString(),
                             Matchers.<SqlParameterSource>anyObject())).thenReturn(0);

        updateGroup();
    }

    @Test(expected = RuntimeException.class)
    public void testUnknownBehaviorUpdate() {

        when(template.update(Matchers.anyString(),
                             Matchers.<SqlParameterSource>anyObject())).thenReturn(2);

        updateGroup();
    }

    @Test(expected = NotFoundException.class)
    public void testNotFoundRemove() {

        when(template.update(Matchers.anyString(),
                             Matchers.<SqlParameterSource>anyObject())).thenReturn(0);

        removeGroup();
    }

    @Test(expected = RuntimeException.class)
    public void testUnknownBehaviorRemove() {

        when(template.update(Matchers.anyString(),
                             Matchers.<SqlParameterSource>anyObject())).thenReturn(2);

        removeGroup();
    }

    protected void createGroup() {
        dao.createGroup(new Event<CreateGroupCommand>(new CreateGroupCommand("Group Name"),
                                                      AuditEvent.now(new User("Test User Name"))));
    }

    protected void updateGroup() {
        dao.updateGroup(new Event<UpdateGroupCommand>(new UpdateGroupCommand(new Identifier<Group>(0L),
                                                                             "Unused"),
                                                      AuditEvent.now(new User("Unused"))));
    }

    protected void removeGroup() {
        dao.removeGroup(new Identifier<Group>(0L));
    }
}
