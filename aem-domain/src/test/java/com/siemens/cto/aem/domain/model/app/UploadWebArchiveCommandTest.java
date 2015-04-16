package com.siemens.cto.aem.domain.model.app;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;

@RunWith(MockitoJUnitRunner.class)
public class UploadWebArchiveCommandTest {

    private Application initApplication(String name, String context, String groupName) {
        return new Application(id(0L, Application.class), name, (String)null, context, new Group(id(0L, Group.class), groupName), true);
    }
    
    private UploadWebArchiveCommand initUploadCommand(Application app, String filename, byte[] data) {
        return initUploadCommand(app, filename, data == null ? 0L : (long)data.length, new ByteArrayInputStream(data));
    }
    private UploadWebArchiveCommand initUploadCommand(Application app, String filename, Long size, InputStream data) {
        UploadWebArchiveCommand uwac = new UploadWebArchiveCommand(
                app,
                filename, 
                size,
                data);
        
        assertSame(app, uwac.getApplication());
        assertEquals(filename, uwac.getFilename());
        assertEquals(size, uwac.getLength());
        assertSame(data, uwac.getTransientData());
        
        return uwac;
    }
    
    @Test
    public void testUploadOk() {
        UploadWebArchiveCommand uwac = initUploadCommand(initApplication("n","c","g"), "file.war", new byte[128]);
        uwac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testUploadFailFilename() {
        UploadWebArchiveCommand uwac = initUploadCommand(initApplication("n","c","g"), null, new byte[128]);
        uwac.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testUploadFailStream() {
        UploadWebArchiveCommand uwac = initUploadCommand(null, "file.war", new byte[0]);
        uwac.validateCommand();
    }

    @Mock
    InputStream badInputStream;
    
    @Test(expected = BadRequestException.class)
    public void testBadStreamThrows() throws IOException { 
                
        Mockito.when(badInputStream.markSupported()).thenReturn(true);
        Mockito.when(badInputStream.read()).thenReturn(-1);
        
        Application app = new Application(null, null, null, null, null, true);
        
        UploadWebArchiveCommand cmd = new UploadWebArchiveCommand(app, "filename.war", 2L, badInputStream);
        cmd.validateCommand(); // should trigger BadRequestException
    }
    

    @Test(expected = BadRequestException.class)
    public void testBadStreamWithoutMarkThrows() throws IOException { 
                
        Mockito.when(badInputStream.markSupported()).thenReturn(false);
        Mockito.when(badInputStream.available()).thenReturn(0);
        
        Application app = new Application(null, null, null, null, null, true);
        
        UploadWebArchiveCommand cmd = new UploadWebArchiveCommand(app, "filename.war", 2L, badInputStream);
        cmd.validateCommand(); // should trigger BadRequestException
    }
}
