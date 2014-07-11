package com.siemens.cto.aem.web.javascript.variable.property;

import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariable;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariableSource;

public class ApplicationPropertySource implements JavaScriptVariableSource {

    private final ApplicationProperties source;

    public ApplicationPropertySource(final ApplicationProperties theSource) {
        source = theSource;
    }

    @Override
    public Set<JavaScriptVariable> createVariables() {
        final Set<JavaScriptVariable> variables = new HashSet<>();
        for (final ApplicationPropertySourceDefinition definition : ApplicationPropertySourceDefinition.values()) {
            variables.add(definition.toVariable(source));
        }
        return variables;
    }
}
