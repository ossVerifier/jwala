package com.siemens.cto.aem.common.request.group;

import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;

public class AddJvmToGroupRequest extends AbstractJvmGroupRequest implements Request {

    public AddJvmToGroupRequest(final Identifier<Group> theGroupId,
                                final Identifier<Jvm> theJvmId) {
        super(theGroupId,
              theJvmId);
    }
}
