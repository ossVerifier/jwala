package com.siemens.cto.aem.common.rule;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;

public class ValidWebArchiveNameRule extends ValidNameRule {

    public ValidWebArchiveNameRule(final String theName) {
        super(theName);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name.endsWith(".war");
    }

    protected MessageResponseStatus getMessageResponseStatus() { return AemFaultType.INVALID_WEB_ARCHIVE_NAME; }

    protected String getMessage() { return "Not a valid web archive filename. Must end in .war"; }
}
