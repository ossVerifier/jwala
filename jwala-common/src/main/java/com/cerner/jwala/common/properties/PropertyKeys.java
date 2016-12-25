package com.cerner.jwala.common.properties;

/**
 * Created by Steven Ger on 12/21/16.
 */
public enum PropertyKeys {

    SCRIPTS_PATH("commands.scripts-path"),
    REMOTE_TOMCAT_DIR_NAME("remote.tomcat.dir.name"),
    REMOTE_PATH_INSTANCES_DIR("remote.paths.instances"),
    REMOTE_PATHS_DEPLOY_DIR("remote.paths.deploy.dir"),
    REMOTE_SCRIPTS("remote.commands.user-scripts"),
    REMOTE_JAVA_HOME("remote.jwala.java.home"),
    REMOTE_JWALA_JAVA_ROOT_DIR("remote.jwala.java.root.dir"),
    REMOTE_PATHS_TOMCAT_ROOT_CORE("remote.paths.tomcat.root.core"),
    REMOTE_PATHS_TOMCAT_CORE("remote.paths.tomcat.core"),
    LOCAL_JWALA_BINARY_DIR("jwala.binary.dir");

    private String propertyName;

    PropertyKeys(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
