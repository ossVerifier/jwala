package com.cerner.jwala.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;

import java.util.List;

public interface ApplicationService {

    Application getApplication(Identifier<Application> aApplicationId);

    Application getApplication(String name);

    Application updateApplication(UpdateApplicationRequest anAppToUpdate, User user);

    Application createApplication(CreateApplicationRequest anAppToCreate, User user);

    void removeApplication(Identifier<Application> anAppIdToRemove, User user);

    List<Application> getApplications();

    List<Application> findApplications(Identifier<Group> groupId);

    List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId);

    List<String> getResourceTemplateNames(final String appName, String jvmName);

    String getResourceTemplate(final String appName, String groupName, String jvmName, final String resourceTemplateName,
                               final ResourceGroup resourceGroup, final boolean tokensReplaced);

    String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName);

    /**
     * Deploy a configuration file.
     *  @param appName              - the application name.
     * @param groupName
     * @param jvmName              - the jvm name where the application resides.
     * @param resourceTemplateName - the resource template in which the configuration file is based on.
     * @param resourceGroup
     * @param user                 - the user.    @return {@link CommandOutput}
     */
    CommandOutput deployConf(String appName, String groupName, String jvmName, String resourceTemplateName, ResourceGroup resourceGroup, User user);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest command);

    /**
     * Gets a preview of a resource file.
     *
     * @param appName   application name
     * @param groupName group name
     * @param jvmName   JVM name
     * @param template  the template to preview.
     * @param resourceGroup
     * @return The resource file preview.
     */
    String previewResourceTemplate(String fileName, String appName, String groupName, String jvmName, String template, ResourceGroup resourceGroup);

    void copyApplicationWarToGroupHosts(Application application);

    void copyApplicationWarToHost(Application application, String hostName);

    void deployApplicationResourcesToGroupHosts(String groupName, Application app, ResourceGroup resourceGroup);

    CommandOutput executeBackUpCommand(String entity, String host, String source) throws CommandFailureException;

    CommandOutput executeCreateDirectoryCommand(String entity, String host, String directoryName) throws CommandFailureException;

    CommandOutput executeSecureCopyCommand(String entity, String host, String source, String destination) throws CommandFailureException;

    CommandOutput executeCheckIfFileExistsCommand(String entity, String host, String fileName) throws CommandFailureException;

    CommandOutput executeChangeFileModeCommand(String entity, String host, String mode, String fileName, String fileOptions) throws CommandFailureException;

    CommandOutput executeUnzipBinaryCommand(String entity, String host, String fileName, String destination, String options) throws CommandFailureException;

    void deployConf(String appName, String hostName, User user);
}
