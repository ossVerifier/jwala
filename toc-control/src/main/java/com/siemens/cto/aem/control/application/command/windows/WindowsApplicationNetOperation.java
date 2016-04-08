package com.siemens.cto.aem.control.application.command.windows;

import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;

public enum WindowsApplicationNetOperation implements ServiceCommandBuilder {

    DEPLOY_WAR(ApplicationControlOperation.DEPLOY_WAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String generatedTarPath = aParams[0];
            final String webAppsDirPath = aParams[1];
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), generatedTarPath, webAppsDirPath);
        }
    },
    DEPLOY_CONFIG_FILE(ApplicationControlOperation.DEPLOY_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String configFilePath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), configFilePath, destPath);
        }
    },
    BACK_UP_CONFIG_FILE(ApplicationControlOperation.BACK_UP_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String srcPath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand("/usr/bin/cp", srcPath, destPath);
        }
    },
    UNPACK_WAR(ApplicationControlOperation.UNPACK_WAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            final String appWarsDirPath = ApplicationProperties.get("stp.webapps.dir");
            final String javaHomePath = ApplicationProperties.get("stp.java.home");

            final String appWarName = aParams[0];
            final String appWarNameDir = appWarName.replace(".war", "");
            final String appWarNameDirBackup = appWarNameDir + "." + dateFormat.format(new Date());

            final String exitWithError = "echo 'EXIT_CODE='1***; echo -n -e '\\xff';";

            return new ExecCommand( new MessageFormat("if [ ! -e \"{0}\" ]; then echo Could not unpack {1}. No such directory {0}; {2} fi;").format(new String[]{appWarsDirPath, appWarName, exitWithError}),
                                    new MessageFormat("if [ ! -e \"{0}\" ]; then echo Could not unpack {1}. No such directory {0}; {2} fi;").format(new String[]{javaHomePath, appWarName, exitWithError}),
                                    new MessageFormat("cd {0};").format(new String[]{appWarsDirPath}),
                                    new MessageFormat("if [ -d {0} ]; then /usr/bin/mv {0} {1}; fi;").format(new String[]{appWarNameDir, appWarNameDirBackup}),
                                    new MessageFormat("/usr/bin/mkdir {0};").format(new String[]{appWarNameDir}),
                                    new MessageFormat("cd {0};").format(new String[]{appWarNameDir}),
                                    new MessageFormat("{0}/bin/jar xf {1}/{2};").format(new String[]{javaHomePath, appWarsDirPath, appWarName}));
        }
    };

    private static final Map<ApplicationControlOperation, WindowsApplicationNetOperation> LOOKUP_MAP = new EnumMap<>(
            ApplicationControlOperation.class);

    static {
        for (final WindowsApplicationNetOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    private final ApplicationControlOperation operation;

    WindowsApplicationNetOperation(final ApplicationControlOperation theOperation) {
        operation = theOperation;
    }

    public static WindowsApplicationNetOperation lookup(final ApplicationControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }

}
