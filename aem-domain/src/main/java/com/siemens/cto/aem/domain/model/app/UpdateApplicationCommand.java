package com.siemens.cto.aem.domain.model.app;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationContextRule;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationNameRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;

public class UpdateApplicationCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<Application> id;
    private final Identifier<Group> newGroupId;
    private final String newContext;
    private final String newName;

    public UpdateApplicationCommand(
            final Identifier<Application> theId,
            final Identifier<Group> theGroupId,
            final String theContext,
            final String theNewName
            ) {
        id = theId;
        newGroupId = theGroupId;
        newName = theNewName;
        newContext = theContext;
    }

    public Identifier<Application> getId() {
        return id;
    }

    public Identifier<Group> getNewGroupId() {
        return newGroupId;
    }

    public String getNewContext() {
        return newContext;
    }
    public String getNewName() {
        return newName;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRuleCommand(new ApplicationIdRule(id),
                                new GroupIdRule(newGroupId),
                                new ApplicationNameRule(newName),
                                new ApplicationContextRule(newContext)).validateCommand();
    }
}
