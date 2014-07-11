package com.siemens.cto.aem.web.javascript.variable.dynamic;

import javax.servlet.http.HttpServletRequest;

import com.siemens.cto.aem.web.javascript.variable.AbstractSingleVariableSource;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariable;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariableSource;

public class LoginStatusSource extends AbstractSingleVariableSource implements JavaScriptVariableSource {

    private final HttpServletRequest request;

    public LoginStatusSource(final HttpServletRequest theRequest) {
        request = theRequest;
    }

    @Override
    protected JavaScriptVariable createSingleVariable() {
        return new JavaScriptVariable("loginStatus",
                                      String.valueOf(request.getParameter("status")));
    }
}
