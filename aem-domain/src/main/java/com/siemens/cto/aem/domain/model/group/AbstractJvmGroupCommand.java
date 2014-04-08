package com.siemens.cto.aem.domain.model.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdRule;

abstract class AbstractJvmGroupCommand implements Command {

    private final Identifier<Group> groupId;
    private final Identifier<Jvm> jvmId;

    public AbstractJvmGroupCommand(final Identifier<Group> theGroupId,
                                   final Identifier<Jvm> theJvmId) {
        groupId = theGroupId;
        jvmId = theJvmId;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public Identifier<Jvm> getJvmId() {
        return jvmId;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRuleCommand(new GroupIdRule(groupId),
                                new JvmIdRule(jvmId)).validateCommand();
    }
}
