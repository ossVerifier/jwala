package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.resource.EntityType;
import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaResourceConfigTemplate;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@Transactional
public abstract class AbstractResourcePersistenceServiceTest {

    public static final String GROUP_TEST_RESOURCE_PERSISTENCE = "group-testResourcePersistence";
    public static final String APP_TEST_RESOURCE_PERSISTENCE = "app-testResourcePersistence";
    public static final String APP_CONTEXT_XML = "app-context.xml";
    @Autowired
    private ResourcePersistenceService resourcePersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Test
    public void testGetApplicationResourceNames() {
        List<String> result = resourcePersistenceService.getApplicationResourceNames(GROUP_TEST_RESOURCE_PERSISTENCE, APP_TEST_RESOURCE_PERSISTENCE);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAppTemplate() {
        // create the group
        CreateGroupRequest createGroupRequest = new CreateGroupRequest(GROUP_TEST_RESOURCE_PERSISTENCE);
        Group group = groupPersistenceService.createGroup(createGroupRequest);

        // create the app
        CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest(group.getId(), APP_TEST_RESOURCE_PERSISTENCE, "/app-context", true, true, false);
        Application app = applicationPersistenceService.createApplication(createApplicationRequest);

        // add the template to the app at the group level
        groupPersistenceService.populateGroupAppTemplate(GROUP_TEST_RESOURCE_PERSISTENCE, APP_TEST_RESOURCE_PERSISTENCE, APP_CONTEXT_XML, "{}", "<root/>");

        String result = resourcePersistenceService.getAppTemplate(GROUP_TEST_RESOURCE_PERSISTENCE, APP_TEST_RESOURCE_PERSISTENCE, APP_CONTEXT_XML);
        assertTrue(!result.isEmpty());
    }
    
    @Test
    public void testCreateResourceAndUpdate() {
        // create the resource
        JpaResourceConfigTemplate result = resourcePersistenceService.createResource(null, null, null, EntityType.EXT_PROPERTIES, "external.properties", new ByteArrayInputStream("property1=one".getBytes()));
        assertEquals("property1=one", result.getTemplateContent());
        assertEquals(EntityType.EXT_PROPERTIES, result.getEntityType());
        assertEquals("external.properties", result.getTemplateName());
        assertEquals(null, result.getEntityId());
        assertEquals(null, result.getAppId());
        assertEquals(null, result.getGroupId());
        assertEquals("{}", result.getMetaData());

        // update the resource
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        ResourceIdentifier identifier = idBuilder.setResourceName("external.properties").build();

        resourcePersistenceService.updateResource(identifier, EntityType.EXT_PROPERTIES, "property1=one11");

    }
}
