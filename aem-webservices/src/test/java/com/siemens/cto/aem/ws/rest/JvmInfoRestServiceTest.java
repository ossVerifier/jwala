package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.service.JvmInfo;
import com.siemens.cto.aem.service.JvmInfoService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmInfoRestServiceTest {

    @Mock
    private JvmInfoService jvmInfoService;

    private JvmInfo jvmInfo;

    @InjectMocks
    private JvmInfoRestService jvmInfoRestService = new JvmInfoRestServiceImpl();

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        jvmInfo = new JvmInfo(1l, "the-jvmInfo-name", "the-jvmfino-hostname");
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
    public void testUpdateJvmInfo() {
        jvmInfoRestService.updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
        verify(jvmInfoService, times(1)).updateJvmInfo(1l, "the-jvmInfo-name", "the-host-name");
    }

    @Test
    public void testDeleteJvm() {
        jvmInfoRestService.deleteJvm(1l);
        verify(jvmInfoService, times(1)).deleteJvm(1l);
    }

}
