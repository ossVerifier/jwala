package com.siemens.cto.aem.web.javascript.variable.dynamic;

import com.siemens.cto.aem.web.javascript.variable.AbstractSingleVariableSource;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariable;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariableSource;
import com.siemens.cto.aem.web.javascript.variable.StringJavaScriptVariable;

import javax.servlet.http.HttpServletRequest;

public class LoginStatusSource extends AbstractSingleVariableSource implements JavaScriptVariableSource {

    private final HttpServletRequest request;

    public LoginStatusSource(final HttpServletRequest theRequest) {
        request = theRequest;
    }

    @Override
    protected JavaScriptVariable createSingleVariable() {
        return new StringJavaScriptVariable("loginStatus",
                                            String.valueOf(request.getParameter("status")));
    }
}
