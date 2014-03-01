package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;
import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.service.exception.RecordNotDeletedException;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.exception.RecordNotUpdatedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmInfoRestServiceImplTest {

    @Mock
    private JvmInfoService jvmInfoService;

    private JvmInfo jvmInfo;

    private JvmInfoRestService jvmInfoRestService;

    private final ObjectMapper mapper = new ObjectMapper();

    private final GroupInfo groupInfo = new GroupInfo(1l, "Group 1");

    @Before
    public void setUp() {
        jvmInfo = new JvmInfo(1l, "the-jvmInfo-name", "the-jvmfino-hostname", groupInfo);
        jvmInfoRestService = new JvmInfoRestServiceImpl(jvmInfoService);
    }

    @Test
    public void testGetJvmInfoById() throws IOException {
        when(jvmInfoService.getJvmInfoById(eq(new Long(1)))).thenReturn(jvmInfo);

        final Response response = jvmInfoRestService.getJvmInfoById(new Long(1));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse =
                (ApplicationResponse) response.getEntity();

        Writer writer = new StringWriter();
        mapper.writeValue(writer, applicationResponse);

        final String jsonStr = writer.toString();
        assertTrue(jsonStr.contains("\"id\":1"));
        assertTrue(jsonStr.contains("\"name\":\"the-jvmInfo-name\""));
        assertTrue(jsonStr.contains("\"host\":\"the-jvmfino-hostname\""));
    }

    @Test
    public void testGetJvmInfoByIdWithException() throws IOException {
        when(jvmInfoService.getJvmInfoById(eq(new Long(1)))).thenThrow(RecordNotFoundException.class);

        final Response response = jvmInfoRestService.getJvmInfoById(new Long(1));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse =
                (ApplicationResponse) response.getEntity();

        Writer writer = new StringWriter();
        mapper.writeValue(writer, applicationResponse);

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"1\""));
    }

    @Test
    public void testGetAllJvmInfo() throws IOException {
        final List<JvmInfo> jvmInfoList = new ArrayList<JvmInfo>();
        jvmInfoList.add(jvmInfo);
        when(jvmInfoService.getAllJvmInfo()).thenReturn(jvmInfoList);

        final Response response = jvmInfoRestService.getAllJvmInfo();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse =
                (ApplicationResponse) response.getEntity();

        Writer writer = new StringWriter();
        mapper.writeValue(writer, applicationResponse);

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"content\":[")); // assert that content is an array
        assertTrue(jsonStr.contains("\"id\":1"));
        assertTrue(jsonStr.contains("\"name\":\"the-jvmInfo-name\""));
        assertTrue(jsonStr.contains("\"host\":\"the-jvmfino-hostname\""));
    }

    @Test
    public void testAddJvmInfo() {
        final Response response  = jvmInfoRestService.addJvmInfo("the-jvmInfo-name", "the-host-name", "the-group-name");
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).addJvmInfo(anyString(), anyString(), any(GroupInfo.class));
    }

    @Test
    @Ignore
    public void testAddJvmInfoWithGroup() {
        // TODO: Implement the test
        throw new UnsupportedOperationException();
    }

    @Test
    public void testFailureToAddJvmInfo() throws IOException {
        doThrow(RecordNotAddedException.class)
                .when(jvmInfoService)
                .addJvmInfo(anyString(), anyString(), any(GroupInfo.class));

        final Response response =
                jvmInfoRestService.addJvmInfo("the-jvmInfo-name", "the-host-name", "the-group-name");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).addJvmInfo(anyString(), anyString(), any(GroupInfo.class));

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"2\""));
    }

    @Test
    public void testFailureToAddJvmInfoWithMissingParams() throws IOException {
        final Response response =
                jvmInfoRestService.addJvmInfo("", "the-host-name", "");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(0)).addJvmInfo(anyString(), anyString(), any(GroupInfo.class));

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"5\""));
        assertTrue(jsonStr.contains("\"message\":\"Invalid parameters: [JVM Name, Group Name]\""));
    }

    @Test
    public void testUpdateJvmInfo() {
        final Response response  = jvmInfoRestService.updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
    }

    @Test
    @Ignore
    public void testUpdateJvmInfoWithGroup() {
        // TODO: Implement the test
        throw new UnsupportedOperationException();
    }

    @Test
    public void testUpdateJvmInfoThatDoesNotExist() throws IOException {
        doThrow(RecordNotFoundException.class)
                .when(jvmInfoService)
                .updateJvmInfo(anyLong(), anyString(), anyString());

        final Response response =
                jvmInfoRestService.updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"1\""));
    }

    @Test
    public void testFailureToUpdateJvmInfo() throws IOException {
        doThrow(RecordNotUpdatedException.class)
                .when(jvmInfoService)
                .updateJvmInfo(anyLong(), anyString(), anyString());

        final Response response =
                jvmInfoRestService.updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"3\""));
    }

    @Test
    public void testDeleteJvm() {
        final Response response = jvmInfoRestService.deleteJvm(1l);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).deleteJvm(1l);
    }

    @Test
    public void testDeleteJvmThatDoesNotExist() throws IOException {
        doThrow(RecordNotFoundException.class).when(jvmInfoService).deleteJvm(eq(1l));

        final Response response = jvmInfoRestService.deleteJvm(1l);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).deleteJvm(1l);

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"1\""));
    }

    @Test
    public void testFailureToDeleteJvm() throws IOException {
        doThrow(RecordNotDeletedException.class).when(jvmInfoService).deleteJvm(eq(1l));

        final Response response = jvmInfoRestService.deleteJvm(1l);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(jvmInfoService, times(1)).deleteJvm(1l);

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"4\""));
    }

}
