package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.resource.ContentType;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.jvm.*;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.toc.files.FileManager;
import groovy.text.SimpleTemplateEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JvmServiceImpl implements JvmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImpl.class);
    private String topicServerStates;
    private static final String DIAGNOSIS_INITIATED = "Diagnosis Initiated on JVM ${jvm.jvmName}, host ${jvm.hostName}";
    private final JvmPersistenceService jvmPersistenceService;
    private final GroupService groupService;
    private final ApplicationService applicationService;
    private final FileManager fileManager;
    private final StateNotificationService stateNotificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupStateNotificationService groupStateNotificationService;
    private final ResourceService resourceService;
    private final ClientFactoryHelper clientFactoryHelper;

    public JvmServiceImpl(final JvmPersistenceService jvmPersistenceService,
                          final GroupService groupService,
                          final ApplicationService applicationService,
                          final FileManager fileManager,
                          final StateNotificationService stateNotificationService,
                          final SimpMessagingTemplate messagingTemplate,
                          final GroupStateNotificationService groupStateNotificationService,
                          final ResourceService resourceService,
                          final ClientFactoryHelper clientFactoryHelper,
                          final String topicServerStates) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.groupService = groupService;
        this.applicationService = applicationService;
        this.fileManager = fileManager;
        this.stateNotificationService = stateNotificationService;
        this.messagingTemplate = messagingTemplate;
        this.groupStateNotificationService = groupStateNotificationService;
        this.resourceService = resourceService;
        this.clientFactoryHelper = clientFactoryHelper;
        this.topicServerStates = topicServerStates;
    }

    @Override
    @Transactional
    public Jvm createJvm(final CreateJvmRequest aCreateJvmRequest,
                         final User aCreatingUser) {
        aCreateJvmRequest.validate();
        return jvmPersistenceService.createJvm(aCreateJvmRequest);
    }

    @Override
    @Transactional
    public Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsRequest aCreateAndAssignRequest,
                                  final User aCreatingUser) {
        // The commands are validated in createJvm() and groupService.addJvmToGroup()
        final Jvm newJvm = createJvm(aCreateAndAssignRequest.getCreateCommand(), aCreatingUser);
        final Set<AddJvmToGroupRequest> addJvmToGroupRequests = aCreateAndAssignRequest.toAddRequestsFor(newJvm.getId());
        addJvmToGroups(addJvmToGroupRequests, aCreatingUser);
        return getJvm(newJvm.getId());
    }

    @Override
    @Transactional
    public void createDefaultTemplates(final String jvmName, Group parentGroup) {
        final String groupName = parentGroup.getName();
        // get the group JVM templates
        List<String> templateNames = groupService.getGroupJvmsResourceTemplateNames(groupName);
        final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
        for (final String templateName : templateNames) {
            String templateContent = groupService.getGroupJvmResourceTemplate(groupName, templateName, resourceService.generateResourceGroup(), false);
            String metaDataStr = groupService.getGroupJvmResourceTemplateMetaData(groupName, templateName);
            try {
                ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
                final UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvm, metaData.getTemplateName(),
                        IOUtils.toInputStream(templateContent), metaDataStr);
                uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());
                jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest);
            } catch (IOException e) {
                LOGGER.error("Failed to map meta data for JVM {} in group {}", jvmName, groupName, e);
                throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to map meta data for JVM " + jvmName + " in group " + groupName, e);
            }
        }

        // get the group App templates
        templateNames = groupService.getGroupAppsResourceTemplateNames(groupName);
        for (String templateName : templateNames) {
            String metaDataStr = groupService.getGroupAppResourceTemplateMetaData(groupName, templateName);
            try {
                ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
                if (metaData.getEntity().getDeployToJvms()) {
                    final Application application = applicationService.getApplication(metaData.getEntity().getTarget());
                    String appTemplate = groupService.getGroupAppResourceTemplate(groupName, application.getName(), templateName,
                            false, new ResourceGroup());
                    UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                            metaData.getDeployFileName(), jvmName, metaDataStr, new ByteArrayInputStream(appTemplate.getBytes()));
                    applicationService.uploadAppTemplate(uploadAppTemplateRequest);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to map meta data while creating JVM for template {} in group {}", templateName, groupName, e);
                throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to map data for template " + templateName + " in group " + groupName, e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final Identifier<Jvm> aJvmId) {
        return jvmPersistenceService.getJvm(aJvmId);
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final String jvmName) {
        return jvmPersistenceService.findJvmByExactName(jvmName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> getJvms() {

        return jvmPersistenceService.getJvms();
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
            LOGGER.info("Adding jvm {} to group {}", command.getJvmId(), command.getGroupId());
            groupService.addJvmToGroup(command, anAddingUser);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public String generateConfigFile(String aJvmName, String templateName) {
        Jvm jvm = jvmPersistenceService.findJvmByExactName(aJvmName);

        final String templateContent = jvmPersistenceService.getJvmTemplate(templateName, jvm.getId());
        if (!templateContent.isEmpty()) {
            return resourceService.generateResourceFile(templateContent, resourceService.generateResourceGroup(), jvmPersistenceService.findJvmByExactName(aJvmName));
        } else {
            throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Failed to find the template in the database or on the file system");
        }
    }

    @Override
    @Transactional
    public String performDiagnosis(Identifier<Jvm> aJvmId) {
        // if the Jvm does not exist, we'll get a 404 NotFoundException
        Jvm jvm = jvmPersistenceService.getJvm(aJvmId);

        pingAndUpdateJvmState(jvm);

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
     * Sets the web server state if the web server is not starting or stopping.
     *
     * @param jvm   the jvm
     * @param state {@link JvmState}
     * @param msg   a message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setState(final Jvm jvm,
                          final JvmState state,
                          final String msg) {
        jvmPersistenceService.updateState(jvm.getId(), state, msg);
        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM));
        groupStateNotificationService.retrieveStateAndSendToATopic(jvm.getId(), Jvm.class);
    }

    @Override
    @Transactional
    public String previewResourceTemplate(String jvmName, String groupName, String template) {
        return resourceService.generateResourceFile(template, resourceService.generateResourceGroup(), jvmPersistenceService.findJvm(jvmName, groupName));
    }

    @Override
    @Transactional
    public JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateRequest uploadJvmTemplateRequest, User user) {
        uploadJvmTemplateRequest.validate();
        return jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(final String jvmName) {
        return jvmPersistenceService.getResourceTemplateNames(jvmName);
    }

    @Override
    @Transactional
    public String getResourceTemplate(final String jvmName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = jvmPersistenceService.getResourceTemplate(jvmName, resourceTemplateName);
        if (tokensReplaced) {
            return resourceService.generateResourceFile(template, resourceService.generateResourceGroup(), jvmPersistenceService.findJvmByExactName(jvmName));
        }
        return template;
    }

    @Override
    public String getResourceTemplateMetaData(String jvmName, String fileName) {
        return jvmPersistenceService.getResourceTemplateMetaData(jvmName, fileName);
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        return jvmPersistenceService.updateResourceTemplate(jvmName, resourceTemplateName, template);
    }

    @Override
    @Transactional
    public String generateInvokeBat(String jvmName) {
        final String invokeBatTemplateContent = fileManager.getResourceTypeTemplate("InvokeBat");
        return resourceService.generateResourceFile(invokeBatTemplateContent, resourceService.generateResourceGroup(), jvmPersistenceService.findJvmByExactName(jvmName));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        jvmPersistenceService.updateState(id, state, "");
        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(id, state, DateTime.now(), StateType.JVM));
    }

    @Override
    @Transactional
    public void pingAndUpdateJvmState(final Jvm jvm) {
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

    @Override
    @Transactional
    public void deployApplicationContextXMLs(Jvm jvm, User user) {
        List<Group> groupList = jvmPersistenceService.findGroupsByJvm(jvm.getId());
        for (Group group : groupList) {
            for (Application app : applicationService.findApplications(group.getId())) {
                for (String templateName : applicationService.getResourceTemplateNames(app.getName(), jvm.getJvmName())) {
                    LOGGER.info("Deploying application xml {} for JVM {} in group {}", templateName, jvm.getJvmName(), group.getName());
                    applicationService.deployConf(app.getName(), group.getName(), jvm.getJvmName(), templateName, resourceService.generateResourceGroup(), user);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getJvmStartedCount(final String groupName) {
        return jvmPersistenceService.getJvmStartedCount(groupName);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getJvmCount(final String groupName) {
        return jvmPersistenceService.getJvmCount(groupName);
    }

    @Override
    public Long getJvmStoppedCount(final String groupName) {
        return jvmPersistenceService.getJvmStoppedCount(groupName);
    }

    @Override
    public Long getJvmForciblyStoppedCount(final String groupName) {
        return jvmPersistenceService.getJvmForciblyStoppedCount(groupName);
    }

    @Override
    public Map<String, String> generateResourceFiles(final String jvmName) throws IOException {
        Map<String, String> generatedFiles = null;
        final List<JpaJvmConfigTemplate> jpaJvmConfigTemplateList = jvmPersistenceService.getConfigTemplates(jvmName);
        final ObjectMapper mapper = new ObjectMapper();
        for (final JpaJvmConfigTemplate jpaJvmConfigTemplate : jpaJvmConfigTemplateList) {
            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            final String resourceTemplateMetaDataString = resourceService.generateResourceFile(jpaJvmConfigTemplate.getMetaData(), resourceGroup, jvm);
            final ResourceTemplateMetaData resourceTemplateMetaData =
                    mapper.readValue(resourceTemplateMetaDataString, ResourceTemplateMetaData.class);
            if (generatedFiles == null) {
                generatedFiles = new HashMap<>();
            }
            if (resourceTemplateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)){
                if (generatedFiles == null) {
                    generatedFiles = new HashMap<>();
                }
                generatedFiles.put(jpaJvmConfigTemplate.getTemplateContent(),
                       resourceTemplateMetaData.getDeployPath() + "/" + resourceTemplateMetaData.getDeployFileName());
            } else {
                final String generatedResourceStr = resourceService.generateResourceFile(jpaJvmConfigTemplate.getTemplateContent(),
                        resourceGroup, jvm);
                generatedFiles.put(createConfigFile(resourceTemplateMetaData.getDeployFileName(), generatedResourceStr),
                        resourceTemplateMetaData.getDeployPath() + "/" + resourceTemplateMetaData.getDeployFileName());
            }
        }
        return generatedFiles;
    }

    /**
     * This method creates a temp file .tpl file, with the generatedResourceString as the input data for the file.
     *
     * @param configFileName          The file name that apprears at the destination.
     * @param generatedResourceString The contents of the file.
     * @return the location of the newly created temp file
     * @throws IOException
     */
    protected String createConfigFile(final String configFileName, String generatedResourceString) throws IOException {
        File templateFile = File.createTempFile(configFileName, ".tpl");
        if (configFileName.endsWith(".bat")) {
            generatedResourceString = generatedResourceString.replaceAll("\n", "\r\n");
        }
        FileUtils.writeStringToFile(templateFile, generatedResourceString);
        return templateFile.getAbsolutePath();
    }
}
