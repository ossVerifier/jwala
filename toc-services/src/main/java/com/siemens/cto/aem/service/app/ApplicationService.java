package com.siemens.cto.aem.service.app;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;

import java.util.List;

public interface ApplicationService {

    Application getApplication(Identifier<Application> aApplicationId);

    Application updateApplication(UpdateApplicationRequest anAppToUpdate, User user);

    Application createApplication(CreateApplicationRequest anAppToCreate, User user);

    void removeApplication(Identifier<Application> anAppIdToRemove, User user);

    List<Application> getApplications();

    List<Application> findApplications(Identifier<Group> groupId);

    List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId);

    Application uploadWebArchive(UploadWebArchiveRequest command, User user);

    Application deleteWebArchive(Identifier<Application> appToRemoveWAR, User user);

    List<String> getResourceTemplateNames(final String appName);

    String getResourceTemplate(final String appName, String groupName, String jvmName, final String resourceTemplateName,
                               final boolean tokensReplaced);

    String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template);

    /**
     * Deploy a configuration file.
     *
     * @param appName              - the application name.
     * @param groupName
     * @param jvmName              - the jvm name where the application resides.
     * @param resourceTemplateName - the resource template in which the configuration file is based on.
     * @param user                 - the user.    @return {@link CommandOutput}
     */
    CommandOutput deployConf(String appName, String groupName, String jvmName, String resourceTemplateName, User user);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest command, User user);

    /**
     * Gets a preview of a resource file.
     *
     * @param appName   application name
     * @param groupName group name
     * @param jvmName   JVM name
     * @param template  the template to preview.
     * @return The resource file preview.
     */
    String previewResourceTemplate(String appName, String groupName, String jvmName, String template);

    void copyApplicationWarToGroupHosts(Application application);
}
