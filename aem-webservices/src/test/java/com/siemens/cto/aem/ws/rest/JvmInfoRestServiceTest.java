package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.persistence.domain.Jvm;
import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.ws.rest.vo.JvmInfoVo;
import com.siemens.cto.aem.ws.rest.vo.JvmInfoVoListWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
    public class JvmInfoRestServiceTest {

    @Mock
    private JvmInfoService jvmInfoService;

    @Mock
    private Jvm jvm;

    @InjectMocks
    private JvmInfoRestService jvmInfoRestService = new JvmInfoRestServiceImpl();

    @Test
    public void testGetJvmInfoById() {
        when(jvmInfoService.getJvmInfoById(eq(new Long(1)))).thenReturn(jvm);
        when(jvm.getId()).thenReturn(1l);
        when(jvm.getName()).thenReturn("the-jvm-name");
        final JvmInfoVo jvmInfoVo = (JvmInfoVo) jvmInfoRestService.getJvmInfoById(new Long(1)).getEntity();
        assertEquals(new Long(1), jvmInfoVo.getId());
        assertEquals("the-jvm-name", jvmInfoVo.getName());
        assertEquals("", jvmInfoVo.getHost());
    }

    @Test
    public void testGetAllJvmInfo() {
        when(jvm.getName()).thenReturn("the-jvm-name");
        final List<Jvm> jvmList = new ArrayList<Jvm>();
        jvmList.add(jvm);
        when(jvmInfoService.getAllJvmInfo()).thenReturn(jvmList);
        JvmInfoVoListWrapper jvmInfoVoListWrapper =
                (JvmInfoVoListWrapper) jvmInfoRestService.getAllJvmInfo().getEntity();
        assertEquals("the-jvm-name", jvmInfoVoListWrapper.getJvmInfoList().get(0).getName());
        assertEquals("", jvmInfoVoListWrapper.getJvmInfoList().get(0).getHost());
    }

    @Test
    public void testAddJvmInfo() {
        jvmInfoRestService.addJvmInfo("the-jvm-name", "the-host-name");
        verify(jvmInfoService, atLeastOnce()).addJvmInfo("the-jvm-name", "the-host-name");
    }

    @Test
    public void testUpdateJvmInfo() {
        jvmInfoRestService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmInfoService, atLeastOnce()).updateJvmInfo(1l, "the-jvm-name", "the-host-name");
    }

    @Test
    public void testDeleteJvm() {
        jvmInfoRestService.deleteJvm(1l);
        verify(jvmInfoService, atLeastOnce()).deleteJvm(1l);
    }

}
