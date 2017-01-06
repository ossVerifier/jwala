package com.cerner.jwala.control.jvm.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */


import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * The CommandFactory class.<br/>
 */
@Component
public class JvmCommandFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmCommandFactory.class);
    private static final  String START_JVM_SERVICE = "start-service.sh";
    private static final  String STOP_JVM_SERVICE = "stop-service.sh";
    private static final  String HEAP_DUMP_JVM_ = "heap-dump.sh";
    private static final  String THREAD_DUMP_JVM= "stop-service.sh";

    private HashMap<String, JvmCommand> commands;

    @Autowired
    protected SshConfiguration sshConfig;
    @Autowired
    protected RemoteCommandExecutorService remoteCommandExecutorService;

    private String remoteJvmInstanceDir = ApplicationProperties.get("remote.paths.instances");

    private String remoteJdkHome = ApplicationProperties.get("remote.jwala.java.home");

    private Boolean dumpLive = ApplicationProperties.getAsBoolean("jmap.dump.live.enabled");

    private String remoteDataDir = ApplicationProperties.get("remote.jwala.data.dir");


    /**
     *
     * @param jvm
     * @param operation
     * @return
     * @throws ApplicationServiceException
     */
    public RemoteCommandReturnInfo executeCommand(Jvm jvm, JvmControlOperation operation) throws ApplicationServiceException{
        if (commands.containsKey(operation.getExternalValue())) {
            return commands.get(operation.getExternalValue()).apply(jvm);
        }
        throw new ApplicationServiceException("JvmCommand not found");
    }

    public void listCommands() {
        LOGGER.debug("Available jvm commands");
        for (String command:commands.keySet()) {
            LOGGER.debug(command);
        }
    }

    /* Factory pattern */
    @PostConstruct
    public void initJvmCommands() {
        commands = new HashMap<>();
        // commands are added here using lambdas. It is also possible to dynamically add commands without editing the code.
        commands.put(JvmControlOperation.START.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommand(START_JVM_SERVICE, jvm))));
        commands.put(JvmControlOperation.STOP.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommand(STOP_JVM_SERVICE, jvm))));
        commands.put(JvmControlOperation.THREAD_DUMP.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForThreadDump(THREAD_DUMP_JVM, jvm))));
        commands.put(JvmControlOperation.HEAP_DUMP.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForHeapDump(HEAP_DUMP_JVM_, jvm))));
        commands.put(JvmControlOperation.DEPLOY_CONFIG_ARCHIVE.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForHeapDump("mkdir", jvm))));
    }

    /**
     *
     * @param jvm
     * @return
     */
    private RemoteSystemConnection getConnection(Jvm jvm) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getPassword(), jvm.getHostName(), sshConfig.getPort());
    }

    /**
     * Get
     * @param jvm
     * @param scriptName
     * @return
     */
    private String getFullPathScript(Jvm jvm, String scriptName){
        return remoteJvmInstanceDir + "/"+jvm.getJvmName()+"/bin/"+scriptName;
    }

    /**
     * Generate parameters for JVM Heap dump
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForHeapDump(String scriptName, Jvm jvm) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
        String dumpFile = "heapDump." + StringUtils.replace(jvm.getJvmName(), " ", "") + "." +fmt.print(DateTime.now());
        String dumpLiveStr = dumpLive ? "live," : "";
        String jvmInstanceDir = remoteJvmInstanceDir + "/" +StringUtils.replace(jvm.getJvmName(), " ", "");
        return new ExecCommand(getFullPathScript(jvm, scriptName),remoteJdkHome, remoteDataDir, dumpFile, dumpLiveStr, jvmInstanceDir);
    }

    /**
     * Generate parameters for Thread dump
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForThreadDump(String scriptName, Jvm jvm) {
        String jvmInstanceDir = remoteJvmInstanceDir + "/" +StringUtils.replace(jvm.getJvmName(), " ", "");
        return new ExecCommand(getFullPathScript(jvm, scriptName),remoteJdkHome, jvmInstanceDir);
    }

    /**
     *
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommand(String scriptName, Jvm jvm){
        return new ExecCommand(getFullPathScript(jvm, scriptName), jvm.getJvmName());
    }
}
