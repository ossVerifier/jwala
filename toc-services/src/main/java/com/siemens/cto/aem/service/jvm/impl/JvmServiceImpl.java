package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.rule.jvm.JvmNameRule;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.aem.template.jvm.TomcatJvmConfigFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import groovy.text.SimpleTemplateEngine;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JvmServiceImpl implements JvmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImpl.class);

    private static final String DIAGNOSIS_INITIATED = "Diagnosis Initiated on JVM ${jvm.jvmName}, host ${jvm.hostName}";

    private final JvmPersistenceService jvmPersistenceService;
    private final GroupService groupService;
    private final FileManager fileManager;
    private final StateNotificationService stateNotificationService;
    private final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc;

    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    public JvmServiceImpl(final JvmPersistenceService theJvmPersistenceService,
                          final GroupService theGroupService,
                          final FileManager theFileManager,
                          final StateNotificationService stateNotificationService,
                          final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc) {
        jvmPersistenceService = theJvmPersistenceService;
        groupService = theGroupService;
        fileManager = theFileManager;
        this.stateNotificationService = stateNotificationService;
        this.grpStateComputationAndNotificationSvc = grpStateComputationAndNotificationSvc;
    }

    @Override
    @Transactional
    public Jvm createJvm(final CreateJvmRequest aCreateJvmRequest,
                         final User aCreatingUser) {

        aCreateJvmRequest.validate();
        // TODO add JVM_NEW state to JVM state table (already done? Appears as NEW on UI)
        return jvmPersistenceService.createJvm(aCreateJvmRequest);
    }

    @Override
    @Transactional
    public Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsRequest aCreateAndAssignRequest,
                                  final User aCreatingUser) {

        //The commands are validated in createJvm() and groupService.addJvmToGroup()

        final Jvm newJvm = createJvm(aCreateAndAssignRequest.getCreateCommand(), aCreatingUser);
        grpStateComputationAndNotificationSvc.computeAndNotify(newJvm.getId(), JvmState.JVM_NEW);

        final Set<AddJvmToGroupRequest> addJvmToGroupRequests = aCreateAndAssignRequest.toAddRequestsFor(newJvm.getId());
        addJvmToGroups(addJvmToGroupRequests, aCreatingUser);

        return getJvm(newJvm.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final Identifier<Jvm> aJvmId) {
        return jvmPersistenceService.getJvm(aJvmId);
    }

    @Override
    @Transactional(readOnly = true)
    public JpaJvm getJpaJvm(final Identifier<Jvm> aJvmId, final boolean fetchGroups) {
        return jvmPersistenceService.getJpaJvm(aJvmId, fetchGroups);
    }

    @Override
    public Jvm getJvm(final String jvmName) {
        return jvmPersistenceService.findJvms(jvmName).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> getJvms() {

        return jvmPersistenceService.getJvms();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final String aJvmNameFragment) {

        new JvmNameRule(aJvmNameFragment).validate();
        return jvmPersistenceService.findJvms(aJvmNameFragment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final Identifier<Group> groupId) {
        return jvmPersistenceService.findJvmsBelongingTo(groupId);
    }

    @Override
    @Transactional
    public Jvm updateJvm(final UpdateJvmRequest updateJvmRequest,
                         final User anUpdatingUser) {

        updateJvmRequest.validate();

        jvmPersistenceService.removeJvmFromGroups(updateJvmRequest.getId());

        addJvmToGroups(updateJvmRequest.getAssignmentCommands(), anUpdatingUser);

        return jvmPersistenceService.updateJvm(updateJvmRequest);
    }

    @Override
    @Transactional
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        jvmPersistenceService.removeJvm(aJvmId);
    }

    protected void addJvmToGroups(final Set<AddJvmToGroupRequest> someAddCommands,
                                  final User anAddingUser) {
        for (final AddJvmToGroupRequest command : someAddCommands) {
            groupService.addJvmToGroup(command,
                    anAddingUser);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public String generateConfigFile(String aJvmName, String templateName) {
        final List<Jvm> jvmList = jvmPersistenceService.findJvms(aJvmName);

        if (jvmList.size() == 1) {
            Jvm jvm = jvmList.get(0);
            final String serverXmlTemplateText = jvmPersistenceService.getJvmTemplate(templateName, jvm.getId());
            if (!serverXmlTemplateText.isEmpty()) {
                return TomcatJvmConfigFileGenerator
                        .getJvmConfigFromText(serverXmlTemplateText, jvm, jvmPersistenceService.getJvms());
            } else {
                throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Failed to find the template in the database or on the file system");
            }

        }
        if (jvmList.size() > 1) {
            throw new BadRequestException(AemFaultType.JVM_NOT_SPECIFIED, "Too many JVMs of the same name");
        } else {
            throw new BadRequestException(AemFaultType.JVM_NOT_FOUND, "JVM not found");
        }
    }

    @Override
    @Transactional
    public String performDiagnosis(Identifier<Jvm> aJvmId) {
        // if the Jvm does not exist, we'll get a 404 NotFoundException
        Jvm jvm = jvmPersistenceService.getJvm(aJvmId);

        pingJvm(jvm);

        SimpleTemplateEngine engine = new SimpleTemplateEngine();
        Map<String, Object> binding = new HashMap<>();
        binding.put("jvm", jvm);

        try {
            return engine.createTemplate(DIAGNOSIS_INITIATED).make(binding).toString();
        } catch (CompilationFailedException | ClassNotFoundException | IOException e) {
            throw new ApplicationException(DIAGNOSIS_INITIATED, e);
            // why do this? Because if there was a problem with the template that made
            // it past initial testing, then it is probably due to the jvm in the binding
            // so just dump out the diagnosis template and the exception so it can be
            // debugged.
        }
    }

    /**
     * Ping the JVM via http get.
     * Used by diagnose button.
     *
     * @param jvm the JVM to ping.
     */
    private void pingJvm(final Jvm jvm) {
        ClientHttpResponse response = null;
        try {
            response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            LOGGER.info(">>> Response = {} from jvm {}", response.getStatusCode(), jvm.getId().getId());
            if (response.getStatusCode() == HttpStatus.OK) {
                setState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
            } else {
                setState(jvm, JvmState.JVM_STOPPED,
                        "Request for '" + jvm.getStatusUri() + "' failed with a response code of '" +
                                response.getStatusCode() + "'");
            }
        } catch (IOException ioe) {
            LOGGER.info(ioe.getMessage(), ioe);
            setState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
        } catch (RuntimeException rte) {
            LOGGER.error(rte.getMessage(), rte);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Sets the web server state if the web server is not starting or stopping.
     *
     * @param jvm   the jvm
     * @param state {@link JvmState}
     * @param msg   a message
     */
    private void setState(final Jvm jvm,
                          final JvmState state,
                          final String msg) {
        jvmPersistenceService.updateState(jvm.getId(), state, msg);
        stateNotificationService.notifyStateUpdated(new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM));
        grpStateComputationAndNotificationSvc.computeAndNotify(jvm.getId(), state);
    }

    /**
     * Sets the jvm state.
     *
     * @param id    the jvm id {@link com.siemens.cto.aem.common.domain.model.id.Identifier}
     * @param state the state {@link JvmState}
     * @param msg   a message
     * @return {@link SetStateRequest}
     */
    private SetStateRequest<Jvm, JvmState> createStateCommand(final Identifier<Jvm> id,
                                                              final JvmState state,
                                                              final String msg) {
        if (StringUtils.isEmpty(msg)) {
            return new JvmSetStateRequest(new CurrentState<>(id,
                    state,
                    DateTime.now(),
                    StateType.JVM));
        }
        return new JvmSetStateRequest(new CurrentState<>(id,
                state,
                DateTime.now(),
                StateType.JVM,
                msg));
    }

    public boolean isJvmStarted(Jvm jvm) {
        return JvmState.JVM_STARTED.equals(jvm.getState());
    }

    @Override
    public String previewResourceTemplate(String jvmName, String groupName, String template) {
        // TODO: Jvm name shouldn't be unique therefore we will have to use the groupName parameter in the future.
        return TomcatJvmConfigFileGenerator.getJvmConfigFromText(template,
                jvmPersistenceService.findJvms(jvmName).get(0),
                jvmPersistenceService.getJvms());
    }

    @Override
    @Transactional
    public JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateRequest uploadJvmTemplateRequest, User user) {
        uploadJvmTemplateRequest.validate();
        return jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest);
    }

    @Override
    public List<String> getResourceTemplateNames(final String jvmName) {
        return jvmPersistenceService.getResourceTemplateNames(jvmName);
    }

    @Override
    public String getResourceTemplate(final String jvmName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = jvmPersistenceService.getResourceTemplate(jvmName, resourceTemplateName);
        if (tokensReplaced) {
            return TomcatJvmConfigFileGenerator.getJvmConfigFromText(template, jvmPersistenceService.findJvms(jvmName).get(0), jvmPersistenceService.getJvms());
        }
        return template;
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        return jvmPersistenceService.updateResourceTemplate(jvmName, resourceTemplateName, template);
    }

    @Override
    public String generateInvokeBat(String jvmName) {
        final List<Jvm> jvmList = jvmPersistenceService.findJvms(jvmName);

        if (jvmList.size() == 1) {
            Jvm jvm = jvmList.get(0);
            return TomcatJvmConfigFileGenerator.getJvmConfigFromText(fileManager.getResourceTypeTemplate("InvokeBat"), jvm, jvmPersistenceService.getJvms());
        }
        if (jvmList.size() > 1) {
            throw new BadRequestException(AemFaultType.JVM_NOT_SPECIFIED, "Too many JVMs of the same name");
        } else {
            throw new BadRequestException(AemFaultType.JVM_NOT_FOUND, "JVM not found");
        }
    }

    @Override
    @Transactional
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        jvmPersistenceService.updateState(id, state);
        stateNotificationService.notifyStateUpdated(new CurrentState<>(id, state, DateTime.now(), StateType.JVM));
        grpStateComputationAndNotificationSvc.computeAndNotify(id, state);
    }

}
