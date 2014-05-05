package com.siemens.cto.aem.control.configuration;

public enum AemSshProperty {

    USER_NAME("userName"),
    PORT("port"),
    PRIVATE_KEY_FILE("privateKeyFile"),
    KNOWN_HOSTS_FILE("knownHostsFile");

    private final String propertyName;

    private AemSshProperty(final String thePropertyName) {
        propertyName = thePropertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
