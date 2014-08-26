package com.siemens.cto.aem.service.app.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.WebArchiveManager;

@RunWith(MockitoJUnitRunner.class)
public class PrivateApplicationServiceImplTest {


    @Mock
    private ApplicationDao applicationDao;

    @Mock
    private ApplicationPersistenceService applicationPersistenceService;
    
    @Mock /*injected*/
    private WebArchiveManager webArchiveManager;

    @InjectMocks @Spy
    private PrivateApplicationService privateApplicationService = new PrivateApplicationServiceImpl();
    
    @Mock
    private Application mockApplication; 
    @Mock
    private Application mockApplication2; 
        
    private Group group;
    private Group group2;
    private Identifier<Group> groupId;
    private Identifier<Group> groupId2;
    
    private ArrayList<Application> applications2 = new ArrayList<>(1);
    
    private User testUser = new User("testUser");

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;
    
    @Before
    public void setUp() {
        
        groupId = new Identifier<Group>(1L);
        groupId2 = new Identifier<Group>(2L);
        group = new Group(groupId, "the-ws-group-name");
        group2 = new Group(groupId2, "the-ws-group-name-2");

        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1L));
        when(mockApplication.getWarPath()).thenReturn("the-ws-group-name/toc-1.0.war");
        when(mockApplication.getName()).thenReturn("TOC 1.0");
        when(mockApplication.getGroup()).thenReturn(group);
        when(mockApplication.getWebAppContext()).thenReturn("/aem");
        

        when(mockApplication2.getId()).thenReturn(new Identifier<Application>(2L));
        when(mockApplication2.getWarPath()).thenReturn("the-ws-group-name-2/toc-1.1.war");
        when(mockApplication2.getName()).thenReturn("TOC 1.1");
        when(mockApplication2.getGroup()).thenReturn(group2);
        when(mockApplication2.getWebAppContext()).thenReturn("/aem");
        
        applications2.add(mockApplication);
        applications2.add(mockApplication2);
        
        ByteBuffer buf = java.nio.ByteBuffer.allocate(2); // 2 byte file
        buf.asShortBuffer().put((short)0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());
    }

    private class IsValidUploadEvent extends ArgumentMatcher<Event<UploadWebArchiveCommand>> {

        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object arg) {
            Event<UploadWebArchiveCommand> event = (Event<UploadWebArchiveCommand>)arg;
            UploadWebArchiveCommand uwac = event.getCommand();
            uwac.validateCommand();
            return true;
        } 
        
    }    
    
    @Test
    public void testUploadWebArchiveData() throws IOException { 
        UploadWebArchiveCommand uwac = new UploadWebArchiveCommand(mockApplication, "fn.war", 2L, uploadedFile);
        Event<UploadWebArchiveCommand> event = Event.create(uwac, AuditEvent.now(testUser));

        when(webArchiveManager.store(argThat(new IsValidUploadEvent()))).thenReturn(RepositoryAction.stored(FileSystems.getDefault().getPath("D:\\fn.war"), 2L));        

        privateApplicationService.uploadWebArchiveData(event);

        Mockito.verify(webArchiveManager, Mockito.times(1)).store(argThat(new IsValidUploadEvent()));        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadWebArchiveUpdateDB() throws IOException { 
        UploadWebArchiveCommand uwac = new UploadWebArchiveCommand(mockApplication, "fn.war", 2L, uploadedFile);        
        Event<UploadWebArchiveCommand> event = Event.create(uwac, AuditEvent.now(testUser));
        
        when(webArchiveManager.store(argThat(new IsValidUploadEvent()))).thenReturn(RepositoryAction.stored(FileSystems.getDefault().getPath("D:\\fn.war"), 2L));
        
        privateApplicationService.uploadWebArchiveUpdateDB(event, RepositoryAction.stored(FileSystems.getDefault().getPath("test.txt"), 0L));

        Mockito.verify(applicationPersistenceService, Mockito.times(1)).updateWARPath(any(Event.class), any(String.class));        
    }

}
