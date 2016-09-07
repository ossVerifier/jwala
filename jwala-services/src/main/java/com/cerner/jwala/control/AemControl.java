package com.cerner.jwala.control;

import com.cerner.jwala.common.properties.ApplicationProperties;

/**
 * Constants specific to this module
 * <code>AemControl.Properties.SCRIPT_PATH.getValue()</code>
 *
 * @author horspe00
 */
public class AemControl {

    private static final String NET_STOP_SLEEP_TIME_SECONDS_DEFAULT = "60";

    private AemControl() {
    }

    public static enum Properties {
        START_SCRIPT_NAME("commands.cygwin.start-service", "start-service.sh"),
        STOP_SCRIPT_NAME("commands.cygwin.stop-service", "stop-service.sh"),
        SCP_SCRIPT_NAME("commands.cygwin.scp", "secure-copy.sh"),
        DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME("commands.cygwin.deploy-config-tar", "deploy-config-tar.sh"),
        DELETE_SERVICE_SCRIPT_NAME("commands.cygwin.delete-service", "delete-service.sh"),
        INVOKE_SERVICE_SCRIPT_NAME("commands.cygwin.invoke-service", "invoke-service.sh"),
        INVOKE_WS_SERVICE_SCRIPT_NAME("commands.cygwin.invoke-ws-service", "invoke-ws-service.sh"),
        UNPACK_WAR_SCRIPT_NAME("commands.cygwin.unpack-war.sh", "unpack-war.sh"),
        SLEEP_TIME("net.stop.sleep.time.seconds", NET_STOP_SLEEP_TIME_SECONDS_DEFAULT);

        private final String propertyName;
        private final String defaultValue;

        Properties(final String thePropertyName, final String theDefaultValue) {
            propertyName = thePropertyName;
            defaultValue = theDefaultValue;
        }

        public String getName() {
            return propertyName;
        }

        public String getDefault() {
            return defaultValue;
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
