package com.siemens.cto.aem.service.resource.impl.handler;

import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaResourceConfigTemplate;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.service.resource.ResourceHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExternalPropertiesResourceHandlerTest {

    private ExternalPropertiesResourceHandler externalPropertiesResourceHandler;

    @Mock
    private ResourceHandler mockSuccessor;

    @Mock
    private ResourceDao mockResourceDao;


    @Before
    public void setup(){
        mockResourceDao = mock(ResourceDao.class);
        mockSuccessor = mock(ResourceHandler.class);
        externalPropertiesResourceHandler = new ExternalPropertiesResourceHandler(mockResourceDao, mockSuccessor);
    }

    @Test
    public void testCanHandle() {
        ResourceIdentifier.Builder resourceIdentifier = new ResourceIdentifier.Builder();
        resourceIdentifier.setGroupName(null);
        resourceIdentifier.setWebAppName(null);
        resourceIdentifier.setJvmName(null);
        resourceIdentifier.setWebServerName(null);
        resourceIdentifier.setResourceName("external.properties");

        assertTrue(externalPropertiesResourceHandler.canHandle(resourceIdentifier.build()));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testDeleteExternalProperties() {
        ResourceIdentifier.Builder resourceIdentifier = new ResourceIdentifier.Builder();
        externalPropertiesResourceHandler.deleteResource(resourceIdentifier.build());
    }

    @Test
    public void testFetchResource() {
        ResourceIdentifier.Builder resourceIdentifierBuilder = new ResourceIdentifier.Builder();
        resourceIdentifierBuilder.setGroupName(null);
        resourceIdentifierBuilder.setWebAppName(null);
        resourceIdentifierBuilder.setJvmName(null);
        resourceIdentifierBuilder.setWebServerName(null);
        resourceIdentifierBuilder.setResourceName("external.properties");

        JpaResourceConfigTemplate mockResourceConfigTemplate = mock(JpaResourceConfigTemplate.class);
        when(mockResourceDao.getExternalPropertiesResource(eq("external.properties"))).thenReturn(mockResourceConfigTemplate);

        externalPropertiesResourceHandler.fetchResource(resourceIdentifierBuilder.build());

        verify(mockResourceDao).getExternalPropertiesResource(eq("external.properties"));
    }

    @Test
    public void testFetchResourcePassesOnToSuccessor() {
        ResourceIdentifier.Builder resourceIdentifierBuilder = new ResourceIdentifier.Builder();
        resourceIdentifierBuilder.setGroupName("test-group-name");
        resourceIdentifierBuilder.setWebAppName("test-app-name");
        resourceIdentifierBuilder.setJvmName(null);
        resourceIdentifierBuilder.setWebServerName(null);
        resourceIdentifierBuilder.setResourceName("external.properties");

        ConfigTemplate mockResourceConfigTemplate = mock(JpaResourceConfigTemplate.class);
        when(mockSuccessor.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockResourceConfigTemplate);

        final ResourceIdentifier resourceId = resourceIdentifierBuilder.build();
        externalPropertiesResourceHandler.fetchResource(resourceId);

        verify(mockSuccessor).fetchResource(eq(resourceId));
    }
}
