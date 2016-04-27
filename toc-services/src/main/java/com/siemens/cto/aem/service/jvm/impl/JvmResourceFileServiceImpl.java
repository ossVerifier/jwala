package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmResourceFileService;
import com.siemens.cto.aem.service.resource.ResourceService;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * {@link JvmResourceFileService} implementation.
 *
 * Created by JC043760 on 4/15/2016.
 */
public class JvmResourceFileServiceImpl implements JvmResourceFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmResourceFileServiceImpl.class);
    private static final String CONFIG_JAR = "_config.jar";
    private final Map<String, ReentrantReadWriteLock> jvmWriteLockMap;
    private final JvmPersistenceService jvmPersistenceService;
    private final JvmControlService jvmControlService;
    private final ResourceService resourceService;
    private final String cmdScriptsPath;
    private final String stpJvmResourcesDir;

    @Autowired
    public JvmResourceFileServiceImpl(final JvmPersistenceService jvmPersistenceService, final JvmControlService jvmControlService,
                                      final ResourceService resourceService, final Map jvmWriteLockMap,
                                      @Value("${commands.scripts-path}") final String cmdScriptsPath,
                                      @Value("${stp.jvm.resources.dir}") final String stpJvmResourcesDir) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.jvmControlService = jvmControlService;
        this.jvmWriteLockMap = jvmWriteLockMap;
        this.resourceService = resourceService;
        this.cmdScriptsPath = cmdScriptsPath;
        this.stpJvmResourcesDir = stpJvmResourcesDir;
    }

    @Override
    public void generateAndDeployFile(String jvmName, String templateName) {

        if (!jvmWriteLockMap.containsKey(jvmName)) {
            jvmWriteLockMap.put(jvmName, new ReentrantReadWriteLock());
        }

        if (jvmWriteLockMap.get(jvmName).writeLock().tryLock()) {
            jvmWriteLockMap.get(jvmName).writeLock().lock();
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            if (!jvm.getState().isStartedState()) {

                try {
                    createScriptsDirectory(jvm);
                    deployScripts(jvm, AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue());
                    deleteJvmWindowsService(jvm);

                    final File targetDir = new File("./" + jvmName);
                    generateResourceFiles(jvm.getJvmName(), targetDir.getAbsolutePath());

                    // Copy the start and stop scripts
                    final File binFile = new File(targetDir.getAbsolutePath() + "/bin");
                    FileUtils.copyFileToDirectory(new File(cmdScriptsPath + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue()),
                            binFile);
                    FileUtils.copyFileToDirectory(new File(cmdScriptsPath + "/" + AemControl.Properties.STOP_SCRIPT_NAME.getValue()),
                            binFile);

                    // Create the log directory
                    FileUtils.forceMkdir(new File(targetDir.getAbsolutePath() + "/log"));

                    // JAR it!
                    final String jarName = jvmName + CONFIG_JAR;
                    final String jarPath = stpJvmResourcesDir + "/" + jarName;
                    FileUtils.forceMkdir(new File(stpJvmResourcesDir));
                    final JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarPath));
                    addToJar(new File("./" + jvmName + "/"), jarOut);
                    jarOut.close();
                } catch (final CommandFailureException | IOException e) {
                    throw new JvmResourceFileServiceException(e);
                }

            } else {
                LOGGER.error("The target JVM {} must be stopped before attempting to update the resource files", jvm.getJvmName());
                throw new JvmResourceFileServiceException("The target JVM must be stopped before attempting to update the resource files");
            }
        } else {
            throw new JvmResourceFileServiceException("Jvm " + jvmName + " is currently being deployed!");
        }
    }

    /**
     * Creates the directory where JVM scripts will reside.
     * @param jvm the {@link Jvm}
     * @throws CommandFailureException
     */
    protected void createScriptsDirectory(final Jvm jvm) throws CommandFailureException {
        final String scriptsDir = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();

        final ExecReturnCode execReturnCode = jvmControlService.createDirectory(jvm, scriptsDir).getReturnCode();
        if (!execReturnCode.wasSuccessful()) {
            LOGGER.error("Creating scripts directory {} failed! Return code = {}.", scriptsDir, execReturnCode.getReturnCode());
            throw new JvmResourceFileServiceException("Create scripts directory failed! Return code = " +
                    execReturnCode.getReturnCode());
        }
    }

    /**
     * Deploy the scripts.
     * @param jvm the {@link Jvm}
     * @param destPath destination
     * @throws CommandFailureException
     * @throws IOException
     */
    protected void deployScripts(final Jvm jvm, final String destPath) throws CommandFailureException, IOException {
        final ControlJvmRequest secureCopyRequest = new ControlJvmRequest(jvm.getId(), JvmControlOperation.SECURE_COPY);
        final String commandsScriptsPath = ApplicationProperties.get("commands.scripts-path");

        final String deployConfigJarPath = commandsScriptsPath + "/" + AemControl.Properties.DEPLOY_CONFIG_TAR_SCRIPT_NAME.getValue();
        final String jvmName = jvm.getJvmName();
        if (!jvmControlService.secureCopyFile(secureCopyRequest, deployConfigJarPath, destPath).getReturnCode().wasSuccessful()){
            LOGGER.error("Secure copy of {} failed during the creation of {}!", deployConfigJarPath, jvmName);
            throw new JvmResourceFileServiceException("Secure copy of " + deployConfigJarPath +
                    " failed during the creation of " + jvmName + "!");
        }

        final String invokeServicePath = commandsScriptsPath + "/" + AemControl.Properties.INVOKE_SERVICE_SCRIPT_NAME.getValue();
        if (!jvmControlService.secureCopyFile(secureCopyRequest, invokeServicePath, destPath).getReturnCode().wasSuccessful()){
            LOGGER.error("Secure copy of {} failed during the creation of {}!", invokeServicePath, jvmName);
            throw new JvmResourceFileServiceException("Secure copy of " + invokeServicePath +
                    " failed during the creation of " + jvmName + "!");
        }

        // Make sure the scripts are executable
        if (!jvmControlService.changeFileMode(jvm, "a+x", destPath, "*.sh").getReturnCode().wasSuccessful()){
            LOGGER.error("Failed to change the file permissions in {} during the creation of {}!", destPath, jvmName);
            // throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to change the file permissions in " + destPath + " during the creation of " + jvmName);
            throw new JvmResourceFileServiceException("Failed to change the file permissions in " + destPath +
                    " during the creation of " + jvmName + "!");
        }
    }

    /**
     * Delete the JVM Windows service.
     * TODO: Support for multiple OSs
     * @param jvm the {@link Jvm}
     */
    protected void deleteJvmWindowsService(final Jvm jvm) {
        if (!jvm.getState().equals(JvmState.JVM_NEW)) {
            // TODO: Provide user parameter!
            CommandOutput commandOutput = jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), null);
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Windows service {} deletion successful!", jvm.getJvmName());
            } else if (ExecReturnCode.STP_EXIT_CODE_SERVICE_DOES_NOT_EXIST == commandOutput.getReturnCode().getReturnCode()) {
                LOGGER.info("No such service found for {} during delete. Continuing with request.", jvm.getJvmName());
            } else {
                final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() :
                        commandOutput.getStandardError();
                LOGGER.error("Deleting windows service {} failed :: ERROR: {}!", jvm.getJvmName(), standardError);
                throw new JvmResourceFileServiceException("Deleting windows service " + jvm.getJvmName() +
                        " failed :: ERROR: " + standardError + "!");
            }
        }
    }

    /**
     * Combines data to the template to come up with resource file(s) which are saved in a file who's location
     * is determined by a path definition found in the meta data of the JVMs configuration template data
     * ({@link JpaJvmConfigTemplate}). This method generates all the resource files associated to a JVM as specified
     * by the JVM name.
     *
     * @param jvmName the JVM name
     * @param destPath the path where the file(s) are saved
     * @throws IOException
     */
    protected void generateResourceFiles(final String jvmName, final String destPath) throws IOException {
        // TODO: Testing, testing, testing!
        final List<JpaJvmConfigTemplate> jpaJvmConfigTemplateList = jvmPersistenceService.getConfigTemplates(jvmName);
        final ObjectMapper mapper = new ObjectMapper();
        for (final JpaJvmConfigTemplate jpaJvmConfigTemplate: jpaJvmConfigTemplateList) {
            final ResourceTemplateMetaData resourceTemplateMetaData =
                    mapper.readValue(jpaJvmConfigTemplate.getMetaData(), ResourceTemplateMetaData.class);

            // TODO: Find out if we need to pass all the JVMs or the JVMs of a certain group!
            // Note: We'll find out when we write a JVM resource file template.
            final String generatedResourceStr = resourceService.generateResourceFile(jpaJvmConfigTemplate.getTemplateContent(),
                    new ResourceGroup(null, jvmPersistenceService.getJvms(), null), jvmPersistenceService.findJvmByExactName(jvmName));

            final String jvmResourcesRelativeDir = destPath + resourceTemplateMetaData.getRelativeDir();
            LOGGER.debug("generating template in location: {}", jvmResourcesRelativeDir + "/", resourceTemplateMetaData.getConfigFileName());

            FileUtils.writeStringToFile(new File(jvmResourcesRelativeDir + "/" + resourceTemplateMetaData.getConfigFileName()),
                                        generatedResourceStr);
        }
    }

    /**
     * Adds source file to jar.
     * // TODO: Review and find out if there's a better way of doing this such as a jar utility or just refactor it.
     * @param source the source file
     * @param target the target output stream.
     * @throws IOException
     */
    private void addToJar(File source, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }

                // TODO: source.listFiles() might return null, refactor this!
                for (final File nestedFile : source.listFiles()) {
                    addToJar(nestedFile, target);
                }
                return;
            }

            JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (in != null)
                in.close();
        }
    }
}
