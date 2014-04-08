package com.siemens.cto.aem.domain.model.group;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class RemoveJvmFromGroupCommand extends AbstractJvmGroupCommand {

    public RemoveJvmFromGroupCommand(final Identifier<Group> theGroupId,
                                     final Identifier<Jvm> theJvmId) {
        super(theGroupId,
              theJvmId
        );
    }

}
