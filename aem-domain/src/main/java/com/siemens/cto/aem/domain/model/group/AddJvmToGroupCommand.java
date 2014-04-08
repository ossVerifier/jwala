package com.siemens.cto.aem.domain.model.group;

import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class AddJvmToGroupCommand extends AbstractJvmGroupCommand implements Command {

    public AddJvmToGroupCommand(final Identifier<Group> theGroupId,
                                final Identifier<Jvm> theJvmId) {
        super(theGroupId,
              theJvmId);
    }
}
