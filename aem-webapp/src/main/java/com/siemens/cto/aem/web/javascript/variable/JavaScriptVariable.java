package com.siemens.cto.aem.web.javascript.variable;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class JavaScriptVariable implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String variableName;
    private final String variableValue;

    public JavaScriptVariable(final String theVariableName,
                              final String theVariableValue) {
        variableName = theVariableName;
        variableValue = theVariableValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        JavaScriptVariable rhs = (JavaScriptVariable) obj;
        return new EqualsBuilder()
                .append(this.variableName, rhs.variableName)
                .append(this.variableValue, rhs.variableValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(variableName)
                .append(variableValue)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("variableName", variableName)
                .append("variableValue", variableValue)
                .toString();
    }
}
