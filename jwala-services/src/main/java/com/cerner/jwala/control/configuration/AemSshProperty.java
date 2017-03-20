package com.cerner.jwala.control.configuration;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;

public enum AemSshProperty {

    USER_NAME(ApplicationProperties.get(PropertyKeys.AEM_SSH_USER_NAME)),
    PORT(ApplicationProperties.get(PropertyKeys.AEM_SSH_PORT)),
    PRIVATE_KEY_FILE(ApplicationProperties.get(PropertyKeys.AEM_SSH_PRIVATE_KEY_FILE)),
    KNOWN_HOSTS_FILE(ApplicationProperties.get(PropertyKeys.AEM_SSH_KNOWN_HOST_FILE)),
    ENCRYPTED_PASSWORD(ApplicationProperties.get(PropertyKeys.AEM_SSH_ENCRYPTED_PASSWORD));

    private final String propertyName;

    private AemSshProperty(final String thePropertyName) {
        propertyName = thePropertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
