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

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
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
    List<Application> emptyList = new ArrayList<Application>(0);
    PaginationParamProvider allProvider = new PaginationParamProvider(null,null, "true");
    @Before
    public void setUp() {
        cut = new ApplicationServiceRestImpl(service);
    }

    @Test
    public void testGetApplications() {
        when(service.getApplications(any(PaginationParameter.class))).thenReturn(applications);
        
        Response resp = cut.getApplications(null, allProvider);
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

    @Test 
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByGroupIdNone() {
        when(service.findApplications(any(Identifier.class), any(PaginationParameter.class))).thenReturn(emptyList);
        Response resp = cut.getApplications(id(2L, Group.class), allProvider);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof List<?>);
        assertEquals(emptyList, entity);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByJvmId() {
        when(service.findApplicationsByJvmId(any(Identifier.class), any(PaginationParameter.class))).thenReturn(emptyList);
        Response resp = cut.findApplicationsByJvmId(id(2L, Jvm.class), allProvider);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof List<?>);
        assertEquals(emptyList, entity);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByNullJvmId() {
        when(service.findApplicationsByJvmId(any(Identifier.class), any(PaginationParameter.class))).thenReturn(emptyList);
        Response resp = cut.findApplicationsByJvmId(null, allProvider);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse)resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof List<?>);
        assertEquals(emptyList, entity);
    }
}
