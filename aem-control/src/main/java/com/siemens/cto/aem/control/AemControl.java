package com.siemens.cto.aem.control;

import com.siemens.cto.aem.common.properties.ApplicationProperties;

/**
 * Constants specific to this module
 * <code>AemControl.Properties.SCRIPT_PATH.getValue()</code>
 *
 * @author horspe00
 */
public class AemControl {

    private static final String NET_STOP_SLEEP_TIME_SECONDS_DEFAULT = "60";

    public static enum Properties {
        CYGPATH("commands.cygwin.cygpath", "/usr/bin/cygpath"),
        SCRIPTS_PATH("commands.scripts-path", "/cygdrive/d/stp/siemens/lib/scripts/"),
        START_SCRIPT_NAME("commands.cygwin.start-service", "start-service.sh"),
        STOP_SCRIPT_NAME("commands.cygwin.stop-service", "stop-service.sh"),
        SCP_SCRIPT_NAME("commands.cygwin.scp", "secure-copy-httpd-conf.sh"),
        TAR_CREATE_COMMAND("commands.cygwin.tar-create", "create-config-tar.sh"),
        DEPLOY_CONFIG_TAR_SCRIPT_NAME("commands.cygwin.deploy-config-tar", "deploy-config-tar.sh"),
        SLEEP_TIME("net.stop.sleep.time.seconds", NET_STOP_SLEEP_TIME_SECONDS_DEFAULT);

        private final String propertyName;
        private final String defaultValue;

        private Properties(final String thePropertyName, final String theDefaultValue) {
            propertyName = thePropertyName;
            defaultValue = theDefaultValue;
        }

        public String getName() {
            return propertyName;
        }

        public String getDefault() {
            return propertyName;
        }

        public String getValue() {
            return ApplicationProperties.get(propertyName, defaultValue);
        }

        @Override
        public String toString() {
            return getValue();
        }
    }
}
