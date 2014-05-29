package com.siemens.cto.aem.domain.model.app;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationContextRule;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationNameRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;

public class CreateApplicationCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private String name;
    private String webAppContext;
    private Identifier<Group> groupId;
    
    public CreateApplicationCommand(Identifier<Group> groupId, String name, String webAppContext) {
        this.name = name;
        this.webAppContext = webAppContext;
        this.groupId = groupId;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }
    public String getWebAppContext() {
        return webAppContext;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new GroupIdRule(groupId),
                                new ApplicationNameRule(name),
                                new ApplicationContextRule(webAppContext)).validate();
    }
}
