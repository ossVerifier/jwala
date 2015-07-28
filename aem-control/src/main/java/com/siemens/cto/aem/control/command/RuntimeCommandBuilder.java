package com.siemens.cto.aem.control.command;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;

import java.util.ArrayList;
import java.util.List;

import static com.siemens.cto.aem.control.AemControl.Properties.CYGPATH;
import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;
import static com.siemens.cto.aem.control.AemControl.Properties.SCRIPTS_PATH;

public class RuntimeCommandBuilder {
    private AemControl.Properties operation;
    private List<String> parameters;
    private static final String PATHS_CYGWIN_BASE = "paths.cygwin.base";

    public RuntimeCommandBuilder() {
        parameters = new ArrayList<String>();
    }

    public void setOperation(AemControl.Properties operation) {
        this.operation = operation;
    }

    public void addParameter(String parameter) {
        this.parameters.add(parameter);
    }

    public RuntimeCommand build() {
        StringBuilder cmmd = new StringBuilder();
        cmmd.append(ApplicationProperties.get(PATHS_CYGWIN_BASE))
                .append("/bin/bash.exe")
                .append(" -c ")
                .append("\"")
                .append(cygpathWrapper(SCP_SCRIPT_NAME));
        for (String param : parameters) {
            cmmd.append(" ");
            cmmd.append(param);
        }
        cmmd.append("\"");
        return new RuntimeCommand(cmmd.toString());
    }

    private static String cygpathWrapper(AemControl.Properties scriptPath) {
        return "`" + CYGPATH.toString() + " " + SCRIPTS_PATH.toString() + scriptPath + "`";
    }

}
