package com.siemens.cto.aem.request.group;

import com.siemens.cto.aem.request.Request;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class AddJvmToGroupRequest extends AbstractJvmGroupRequest implements Request {

    public AddJvmToGroupRequest(final Identifier<Group> theGroupId,
                                final Identifier<Jvm> theJvmId) {
        super(theGroupId,
              theJvmId);
    }
}
