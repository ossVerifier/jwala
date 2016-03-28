package com.siemens.cto.aem.common.domain.model.app;

import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.group.Group;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.siemens.cto.aem.common.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class UploadWebArchiveRequestTest {

    private Application initApplication(String name, String context, String groupName) {
        return new Application(id(0L, Application.class), name, (String)null, context, new Group(id(0L, Group.class), groupName), true, true, false, "testWar.war");
    }
    
    private UploadWebArchiveRequest initUploadCommand(Application app, String filename, byte[] data) {
        return initUploadCommand(app, filename, data == null ? 0L : (long)data.length, new ByteArrayInputStream(data));
    }
    private UploadWebArchiveRequest initUploadCommand(Application app, String filename, Long size, InputStream data) {
        UploadWebArchiveRequest uwac = new UploadWebArchiveRequest(
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
        UploadWebArchiveRequest uwac = initUploadCommand(initApplication("n","c","g"), "file.war", new byte[128]);
        uwac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testUploadFailFilename() {
        UploadWebArchiveRequest uwac = initUploadCommand(initApplication("n","c","g"), null, new byte[128]);
        uwac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testUploadFailStream() {
        UploadWebArchiveRequest uwac = initUploadCommand(null, "file.war", new byte[0]);
        uwac.validate();
    }

    @Mock
    InputStream badInputStream;
    
    @Test(expected = BadRequestException.class)
    public void testBadStreamThrows() throws IOException { 
                
        Mockito.when(badInputStream.markSupported()).thenReturn(true);
        Mockito.when(badInputStream.read()).thenReturn(-1);
        
        Application app = new Application(null, null, null, null, null, true, true, false, null);
        
        UploadWebArchiveRequest cmd = new UploadWebArchiveRequest(app, "filename.war", 2L, badInputStream);
        cmd.validate(); // should trigger BadRequestException
    }
    

    @Test(expected = BadRequestException.class)
    public void testBadStreamWithoutMarkThrows() throws IOException { 
                
        Mockito.when(badInputStream.markSupported()).thenReturn(false);
        Mockito.when(badInputStream.available()).thenReturn(0);
        
        Application app = new Application(null, null, null, null, null, true, true, false, null);
        
        UploadWebArchiveRequest cmd = new UploadWebArchiveRequest(app, "filename.war", 2L, badInputStream);
        cmd.validate(); // should trigger BadRequestException
    }
}
