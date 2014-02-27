package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.service.JvmInfo;
import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotUpdatedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmInfoRestServiceImplTest {

    @Mock
    private JvmInfoService jvmInfoService;

    private JvmInfo jvmInfo;

    private JvmInfoRestService jvmInfoRestService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        jvmInfo = new JvmInfo(1l, "the-jvmInfo-name", "the-jvmfino-hostname");
        jvmInfoRestService = new JvmInfoRestServiceImpl(jvmInfoService);
    }

    @Test
    public void testGetJvmInfoById() throws IOException {
        when(jvmInfoService.getJvmInfoById(eq(new Long(1)))).thenReturn(jvmInfo);

        ApplicationResponse applicationResponse =
                (ApplicationResponse) jvmInfoRestService.getJvmInfoById(new Long(1)).getEntity();

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

        ApplicationResponse applicationResponse =
                (ApplicationResponse) jvmInfoRestService.getJvmInfoById(new Long(1)).getEntity();

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

        ApplicationResponse applicationResponse =
                (ApplicationResponse) jvmInfoRestService.getAllJvmInfo().getEntity();

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
        jvmInfoRestService.addJvmInfo("the-jvmInfo-name", "the-host-name");
        verify(jvmInfoService, times(1)).addJvmInfo("the-jvmInfo-name", "the-host-name");
    }

    @Test
    public void testFailureToAddJvmInfo() throws IOException {
        doThrow(RecordNotAddedException.class)
                .when(jvmInfoService)
                .addJvmInfo(anyString(), anyString());

        Response response =
                jvmInfoRestService.addJvmInfo("the-jvmInfo-name", "the-host-name");
        verify(jvmInfoService, times(1)).addJvmInfo("the-jvmInfo-name", "the-host-name");

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"2\""));
    }

    @Test
    public void testUpdateJvmInfo() {
        jvmInfoRestService.updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
        verify(jvmInfoService, times(1)).updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
    }

    @Test
    public void testFailureToUpdateJvmInfo() throws IOException {
        doThrow(RecordNotUpdatedException.class)
                .when(jvmInfoService)
                .updateJvmInfo(anyLong(), anyString(), anyString());

        Response response =
                jvmInfoRestService.updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
        verify(jvmInfoService, times(1)).updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");

        Writer writer = new StringWriter();
        mapper.writeValue(writer, response.getEntity());

        final String jsonStr = writer.toString();

        assertTrue(jsonStr.contains("\"msgCode\":\"3\""));
    }

    @Test
    public void testDeleteJvm() {
        jvmInfoRestService.deleteJvm(1l);
        verify(jvmInfoService, times(1)).deleteJvm(1l);
    }

}
