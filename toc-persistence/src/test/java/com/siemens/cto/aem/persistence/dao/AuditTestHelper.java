package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.user.User;

public class AuditTestHelper {


    public static AuditEvent createAuditEvent(final String aUserId) {
        return AuditEvent.now(new User(aUserId));
    }

}
