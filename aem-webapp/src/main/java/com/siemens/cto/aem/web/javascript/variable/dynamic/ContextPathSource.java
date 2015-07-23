package com.siemens.cto.aem.web.javascript.variable.dynamic;

import com.siemens.cto.aem.web.javascript.variable.AbstractSingleVariableSource;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariable;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariableSource;
import com.siemens.cto.aem.web.javascript.variable.StringJavaScriptVariable;

import javax.servlet.ServletContext;

public class ContextPathSource extends AbstractSingleVariableSource implements JavaScriptVariableSource {

    private final ServletContext servletContext;

    public ContextPathSource(final ServletContext theServletContext) {
        servletContext = theServletContext;
    }

    @Override
    protected JavaScriptVariable createSingleVariable() {
        return new StringJavaScriptVariable("contextPath",
                                            servletContext.getContextPath());
    }
}
