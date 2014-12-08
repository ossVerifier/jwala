package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;

import java.net.URI;
import java.net.URISyntaxException;

public class HostNameRule extends ValidNameRule {

    public HostNameRule(final String theName) {
        super(theName);
    }

    @Override
    public MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_HOST_NAME;
    }

    @Override
    public String getMessage() {
        return "Invalid Host Name : \"" + name + "\"";
    }

    @Override
    public boolean isValid() {
        if (super.isValid()) {
            try {
                new URI("http", null, name, 8080, "/somePath", "", "");
            } catch (URISyntaxException e) {
                return false;
            }
            return true;
        }
        return false;
    }

}
