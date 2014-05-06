package com.siemens.cto.aem.domain.model.app;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationContextRule;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationNameRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;

public class CreateApplicationCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private String name;
    private String context;
    private Identifier<Group> groupId;
    
    public CreateApplicationCommand(Identifier<Group> groupId, String name, String context) {
        this.name = name;
        this.context = context;
        this.groupId = groupId;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }
    public String getContext() {
        return context;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRuleCommand(new GroupIdRule(groupId),
                                new ApplicationNameRule(name),
                                new ApplicationContextRule(context)).validateCommand();
    }
}
