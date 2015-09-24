package com.siemens.cto.aem.service.app;

import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;

import java.util.List;

public interface ApplicationService {

    Application getApplication(Identifier<Application> aApplicationId);

    Application updateApplication(UpdateApplicationCommand anAppToUpdate, User user);
    Application createApplication(CreateApplicationCommand anAppToCreate, User user);
    void removeApplication(Identifier<Application> anAppIdToRemove, User user);

    List<Application> getApplications();

    List<Application> findApplications(Identifier<Group> groupId);

    List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId);

    Application uploadWebArchive(UploadWebArchiveCommand command, User user);

    Application deleteWebArchive(Identifier<Application> appToRemoveWAR, User user);

    List<String> getResourceTemplateNames(final String appName);

    String getResourceTemplate(final String appName, String groupName, String jvmName, final String resourceTemplateName,
                               final boolean tokensReplaced);

    String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template);

    /**
     * Deploy a configuration file.
     * @param appName - the application name.
     * @param groupName
     * @param jvmName - the jvm name where the application resides.
     * @param resourceTemplateName - the resource template in which the configuration file is based on.
     * @param user - the user.    @return {@link com.siemens.cto.aem.domain.model.exec.ExecData}
     */
    ExecData deployConf(String appName, String groupName, String jvmName, String resourceTemplateName, User user);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateCommand command, User user);	
    /**
     * Gets a preview of a resource file.
     * @param appName application name
     * @param groupName group name
     * @param jvmName JVM name
     * @param template the template to preview.
     * @return The resource file preview.
     */
    String previewResourceTemplate(String appName, String groupName, String jvmName, String template);
}
