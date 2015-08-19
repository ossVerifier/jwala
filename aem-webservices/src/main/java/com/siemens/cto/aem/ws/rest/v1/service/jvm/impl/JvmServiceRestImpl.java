package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.JvmIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.siemens.cto.aem.control.AemControl.Properties.TAR_CREATE_COMMAND;

public class JvmServiceRestImpl implements JvmServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceRestImpl.class);

    private final JvmService jvmService;
    private final JvmControlService jvmControlService;
    private final StateService<Jvm, JvmState> jvmStateService;
    private Map<String, ReentrantReadWriteLock> wsWriteLocks;

    public JvmServiceRestImpl(final JvmService theJvmService,
                              final JvmControlService theJvmControlService,
                              final StateService<Jvm, JvmState> theJvmStateService) {
        jvmService = theJvmService;
        jvmControlService = theJvmControlService;
        jvmStateService = theJvmStateService;
        wsWriteLocks = new HashMap<>();
    }

    @Override
    public Response getJvms() {
        LOGGER.debug("Get JVMs requested");
        final List<Jvm> jvms = jvmService.getJvms();
        return ResponseBuilder.ok(jvms);
    }

    @Override
    public Response getJvm(final Identifier<Jvm> aJvmId) {
        LOGGER.debug("Get JVM requested: {}", aJvmId);
        return ResponseBuilder.ok(jvmService.getJvm(aJvmId));
    }

    @Override
    public Response createJvm(final JsonCreateJvm aJvmToCreate,
                              final AuthenticatedUser aUser) {
        LOGGER.debug("Create JVM requested: {}", aJvmToCreate);
        final User user = aUser.getUser();

        final Jvm jvm;
        if (aJvmToCreate.areGroupsPresent()) {
            jvm = jvmService.createAndAssignJvm(aJvmToCreate.toCreateAndAddCommand(),
                    user);
        } else {
            jvm = jvmService.createJvm(aJvmToCreate.toCreateJvmCommand(),
                    user);
        }
        return ResponseBuilder.created(jvm);
    }

    @Override
    public Response updateJvm(final JsonUpdateJvm aJvmToUpdate,
                              final AuthenticatedUser aUser) {
        LOGGER.debug("Update JVM requested: {}", aJvmToUpdate);
        return ResponseBuilder.ok(jvmService.updateJvm(aJvmToUpdate.toUpdateJvmCommand(),
                aUser.getUser()));
    }

    @Override
    public Response removeJvm(final Identifier<Jvm> aJvmId) {
        //TODO This needs to be audited
        LOGGER.debug("Delete JVM requested: {}", aJvmId);
        jvmService.removeJvm(aJvmId);
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlJvm(final Identifier<Jvm> aJvmId,
                               final JsonControlJvm aJvmToControl,
                               final AuthenticatedUser aUser) {
        LOGGER.debug("Control JVM requested: {} {}", aJvmId, aJvmToControl);
        final JvmControlHistory controlHistory = jvmControlService.controlJvm(new ControlJvmCommand(aJvmId, aJvmToControl.toControlOperation()),
                aUser.getUser());
        final ExecData execData = controlHistory.getExecData();
        if (execData.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(controlHistory);
        } else {
            throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL,
                    execData.getStandardError());
        }
    }

    @Override
    public Response getCurrentJvmStates(final JvmIdsParameterProvider aJvmIdsParameterProvider) {
        LOGGER.debug("Current JVM states requested : {}", aJvmIdsParameterProvider);
        final Set<Identifier<Jvm>> jvmIds = aJvmIdsParameterProvider.valueOf();
        final Set<CurrentState<Jvm, JvmState>> currentJvmStates;

        if (jvmIds.isEmpty()) {
            currentJvmStates = jvmStateService.getCurrentStates();
        } else {
            currentJvmStates = jvmStateService.getCurrentStates(jvmIds);
        }

        return ResponseBuilder.ok(currentJvmStates);
    }

    @Override
    public Response generateConfig(String aJvmName) {
        try {
            String serverXmlStr = jvmService.generateConfig(aJvmName);
            return Response.ok(serverXmlStr).build();
        } catch (TemplateNotFoundException e) {
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND,
                    e.getMessage(),
                    e);
        }
    }

    @Override
    public Response generateAndDeployConfig(final Identifier<Jvm> aJvmId, AuthenticatedUser user) {

        // only one at a time per web server
        if (!wsWriteLocks.containsKey(aJvmId.toString())) {
            wsWriteLocks.put(aJvmId.toString(), new ReentrantReadWriteLock());
        }
        wsWriteLocks.get(aJvmId.toString()).writeLock().lock();

        // create the file
        Jvm jvm = jvmService.getJvm(aJvmId);
        String jvmName = jvm.getJvmName();
        final String jvmConfigTar = generateJvmConfigTar(jvmName, new RuntimeCommandBuilder());

        // copy and deploy the tar file
        ExecData execData;

        try {
            // copy
            execData = jvmService.secureCopyConfigTar(jvm, new RuntimeCommandBuilder());
            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.info("Copy of config tar successful: {}", jvmConfigTar);
            } else {
                String standardError = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                LOGGER.error("Copy command completed with error trying to copy config tar to {} :: ERROR: {}", jvmName, standardError);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
            }

            // call script to backup and tar the current directory and then untar the new tar
            JvmControlHistory completeHistory = jvmControlService.controlJvm(new ControlJvmCommand(aJvmId, JvmControlOperation.DEPLOY_CONFIG_TAR), user.getUser());
            execData = completeHistory.getExecData();
            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.info("Deployment of config tar was successful: {}", jvmConfigTar);
            } else {
                String standardError = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                LOGGER.error("Deploy command completed with error trying to extract and back up JVM config {} :: ERROR: {}", jvmName, standardError);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
            }
        } catch (CommandFailureException e) {
            LOGGER.error("Failed to copy the httpd.conf to {} :: ERROR: {}", jvmName, e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to copy httpd.conf", e);
        } finally {
            wsWriteLocks.get(aJvmId.toString()).writeLock().unlock(); // potential memory leak: could clean it up but adds complexity
        }
        return ResponseBuilder.ok();
    }

    private String generateJvmConfigTar(String jvmName, RuntimeCommandBuilder rtCommandBuilder) {

        // TODO eventually replace all this with generating the config files and creating the tar file to be copied over and expanded
        String serverXmlStr = jvmService.generateConfig(jvmName);

        // TODO create test file for now but really create all the conf files
        String stpJvmResourcesDir = ApplicationProperties.get("stp.jvm.resources.dir");
        String jvmResourcesDir = stpJvmResourcesDir + "/" + jvmName;
        File resDir = new File(jvmResourcesDir);
        if (!resDir.exists()) {
            boolean result = resDir.mkdirs();
            if (!result){
                throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to create directory" + jvmResourcesDir);
            }
        }
        File serverXml = new File(jvmResourcesDir + "/JM_resourceTEST.xml"); // TODO replace with real conf file
        try {
            FileUtils.writeStringToFile(serverXml, serverXmlStr);
        } catch (FileNotFoundException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to create file " + serverXml.getPath());
        } catch (IOException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to write file " + serverXml.getPath());
        }

        // tar up the test file
        rtCommandBuilder.setOperation(TAR_CREATE_COMMAND);
        String jvmConfigTar = jvmName + "_config.tar";
        String configTar = stpJvmResourcesDir + "/" + jvmName;
        rtCommandBuilder.addParameter(jvmConfigTar);
        rtCommandBuilder.addCygwinPathParameter(configTar);
        RuntimeCommand tarCommand = rtCommandBuilder.build();
        ExecData tarResult = tarCommand.execute();
        if (!tarResult.getReturnCode().wasSuccessful()) {
            String standardError = tarResult.getStandardError().isEmpty() ? tarResult.getStandardOutput() : tarResult.getStandardError();
            LOGGER.error("Tar create command completed with error trying to create config tar for {} :: ERROR: {}", jvmName, standardError);
            throw new InternalErrorException(AemFaultType.INVALID_PATH, standardError);
        }

        return configTar;
    }

    @Override
    public Response diagnoseJvm(Identifier<Jvm> aJvmId) {

        String diagnosis = jvmService.performDiagnosis(aJvmId);

        return Response.ok(diagnosis).build();
    }

    @Override
    public Response getResourceNames(final String jvmName) {
        // TODO: Get resource names from db.
        final List<String> resources = new LinkedList<>();
        resources.add("context.xml");
        resources.add("server.xml");
        resources.add("setenv.bat");
        return ResponseBuilder.ok(resources);
    }

}
