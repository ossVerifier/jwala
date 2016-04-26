package com.siemens.cto.aem.web.javascript.variable.property;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariable;
import com.siemens.cto.aem.web.javascript.variable.StringJavaScriptVariable;
import org.apache.commons.lang3.builder.ToStringBuilder;

public enum ApplicationPropertySourceDefinition {

    LOAD_BALANCER_STATUS_MOUNT("loadBalancerStatusMount", "mod_jk.load-balancer.status.mount", "/balancer-manager", VariableStyle.STRING),
    STATE_POLL_TIMEOUT("statePollTimeout", "state.poll.timeout", "1000", VariableStyle.STRING),
    START_STOP_TIMEOUT("startStopTimeout", "start.stop.timeout", "180000", VariableStyle.STRING),
    HEALTH_CHECK_APP("healthCheckApp", "health.check.app", "/hct", VariableStyle.STRING),
    HISTORY_MAX_READ_REC_COUNT("historyReadMaxRecCount", "history.max-read-rec-count", "30", VariableStyle.STRING),
    RESOURCES_ENABLED("resourcesEnabled", "resources.enabled", "true", VariableStyle.STRING),
    OPS_GRP_CHILDREN_VIEW_OPEN("opsGrpChildrenViewOpen", "operations.group.children.view.open", "true", VariableStyle.STRING);

    private final String variableName;
    private final String propertyKey;
    private final String defaultValue;
    private final VariableStyle style;

    ApplicationPropertySourceDefinition(final String theVariableName,
                                                final String thePropertyKey,
                                                final String theDefaultValue,
                                                final VariableStyle theStyle) {
        variableName = theVariableName;
        propertyKey = thePropertyKey;
        defaultValue = theDefaultValue;
        style = theStyle;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public JavaScriptVariable toVariable(final ApplicationProperties aPropertySource) {
        return style.create(variableName,
                            getPropertyValueOrDefault(aPropertySource));
    }

    protected String getPropertyValueOrDefault(final ApplicationProperties aPropertySource) {
        final String value = ApplicationProperties.get(propertyKey);
        if (isValidProperty(value)) {
            return value;
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("variableName", variableName)
                .append("propertyKey", propertyKey)
                .append("defaultValue", defaultValue)
                .toString();
    }

    private enum VariableStyle {
        STRING {
            @Override
            public JavaScriptVariable create(final String aVariableName,
                                             final String aVariableValue) {
                return new StringJavaScriptVariable(aVariableName,
                                                    aVariableValue);
            }
        },
        RAW {
            @Override
            public JavaScriptVariable create(final String aVariableName,
                                             final String aVariableValue) {
                return new JavaScriptVariable(aVariableName,
                                              aVariableValue);
            }
        };

        public abstract JavaScriptVariable create(final String aVariableName,
                                                  final String aVariableValue);
    }

    private boolean isValidProperty(final String aValue) {
        return aValue != null;
    }
}
