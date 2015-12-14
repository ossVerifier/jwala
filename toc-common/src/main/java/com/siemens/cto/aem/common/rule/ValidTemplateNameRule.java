package com.siemens.cto.aem.common.rule;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;

/**
 * Created by z0033r5b on 8/20/2015.
 */
public class ValidTemplateNameRule extends ValidNameRule {

    public ValidTemplateNameRule(final String theName) {
        super(theName);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name.endsWith(".tpl");
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_TEMPLATE_NAME;
    }

    @Override
    protected String getMessage() {
        return "Not a valid template filename. Must end in .tpl";
    }
}
