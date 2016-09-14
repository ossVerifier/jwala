package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.app.UploadWebArchiveRequest;
import com.cerner.jwala.files.RepositoryFileInformation;
import com.cerner.jwala.files.WebArchiveManager;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.service.app.PrivateApplicationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrivateApplicationServiceImplTest {

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
        when(mockApplication.getWarPath()).thenReturn("the-ws-group-name/jwala-1.0.war");
        when(mockApplication.getName()).thenReturn("jwala 1.0");
        when(mockApplication.getGroup()).thenReturn(group);
        when(mockApplication.getWebAppContext()).thenReturn("/jwala");
        

        when(mockApplication2.getId()).thenReturn(new Identifier<Application>(2L));
        when(mockApplication2.getWarPath()).thenReturn("the-ws-group-name-2/jwala-1.1.war");
        when(mockApplication2.getName()).thenReturn("jwala 1.1");
        when(mockApplication2.getGroup()).thenReturn(group2);
        when(mockApplication2.getWebAppContext()).thenReturn("/jwala");
        
        applications2.add(mockApplication);
        applications2.add(mockApplication2);
        
        ByteBuffer buf = java.nio.ByteBuffer.allocate(2); // 2 byte file
        buf.asShortBuffer().put((short)0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());
    }
    
    @Test
    public void testUploadWebArchiveData() throws IOException {
        UploadWebArchiveRequest uwac = new UploadWebArchiveRequest(mockApplication, "fn.war", 2L, uploadedFile);

        when(webArchiveManager.store(uwac)).thenReturn(RepositoryFileInformation.stored(FileSystems.getDefault().getPath("D:\\fn.war"), 2L));

        privateApplicationService.uploadWebArchiveData(uwac);

        Mockito.verify(webArchiveManager, Mockito.times(1)).store(uwac);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadWebArchiveUpdateDB() throws IOException { 
        UploadWebArchiveRequest uwac = new UploadWebArchiveRequest(mockApplication, "fn.war", 2L, uploadedFile);

        when(webArchiveManager.store(any(UploadWebArchiveRequest.class))).thenReturn(RepositoryFileInformation.stored(FileSystems.getDefault().getPath("D:\\fn.war"), 2L));
        
        privateApplicationService.uploadWebArchiveUpdateDB(uwac, RepositoryFileInformation.stored(FileSystems.getDefault().getPath("test.txt"), 0L));

        Mockito.verify(applicationPersistenceService, Mockito.times(1)).updateWARPath(any(UploadWebArchiveRequest.class), any(String.class));
    }

}
