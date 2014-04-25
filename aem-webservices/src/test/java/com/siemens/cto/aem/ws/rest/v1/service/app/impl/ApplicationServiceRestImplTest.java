package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceRestImplTest {

    @Mock
    private ApplicationService service;
    
    private ApplicationServiceRest cut;

    Application application = new Application();
    List<Application> applications = new ArrayList<Application>(1);
    
    @Before
    public void setUp() {
        cut = new ApplicationServiceRestImpl(service);
    }

    @Test
    public void testGetApplications() {
        when(service.getApplications(any(PaginationParameter.class))).thenReturn(applications);
        PaginationParamProvider provider = new PaginationParamProvider(null,null, "true");
        Response resp = cut.getApplications(provider);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof List<?>);
        assertEquals(applications, entity);
    }
    
    @Test 
    @SuppressWarnings("unchecked")
    public void testGetApplicationById() {
        when(service.getApplication(any(Identifier.class))).thenReturn(application);
        Response resp = cut.getApplication(id(1L, Application.class));
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof Application);
        assertEquals(application, entity);
    }

    
}
