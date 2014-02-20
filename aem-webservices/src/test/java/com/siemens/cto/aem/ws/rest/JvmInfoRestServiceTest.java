package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.persistence.domain.Jvm;
import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.ws.rest.vo.JvmInfoVo;
import com.siemens.cto.aem.ws.rest.vo.JvmInfoVoListWrapper;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class JvmInfoRestServiceTest extends TestCase {

    private JvmInfoService jvmInfoService;
    private Jvm jvm;
    private JvmInfoRestService jvmInfoRestService;

    @Override
    protected void setUp() throws Exception {
        jvmInfoService = mock(JvmInfoService.class);
        jvm = mock(Jvm.class);
        jvmInfoRestService = new JvmInfoRestServiceImpl(jvmInfoService);
    }

    public void testGetJvmInfoById() {
        when(jvm.getName()).thenReturn("Test");
        when(jvmInfoService.getJvmInfoById(anyLong())).thenReturn(jvm);
        final JvmInfoVo jvmInfoVo = (JvmInfoVo) jvmInfoRestService.getJvmInfoById(new Long(1)).getEntity();
        assertEquals("Test", jvmInfoVo.getName());
    }

    public void testGetAllJvmInfo() {
        when(jvm.getName()).thenReturn("Test");
        final List<Jvm> jvmList = new ArrayList<Jvm>();
        jvmList.add(jvm);
        when(jvmInfoService.getAllJvmInfo()).thenReturn(jvmList);
        JvmInfoVoListWrapper jvmInfoVoListWrapper =
                (JvmInfoVoListWrapper) jvmInfoRestService.getAllJvmInfo().getEntity();
        assertEquals("Test", jvmInfoVoListWrapper.getJvmInfoList().get(0).getName());
    }

    public void testAddJvmInfo() {
        jvmInfoRestService.addJvmInfo("the-jvm-name", "the-host-name");
        verify(jvmInfoService, atLeastOnce()).addJvmInfo("the-jvm-name", "the-host-name");
    }

    public void testUpdateJvmInfo() {
        jvmInfoRestService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmInfoService, atLeastOnce()).updateJvmInfo(1l, "the-jvm-name", "the-host-name");
    }

    public void testDeleteJvm() {
        jvmInfoRestService.deleteJvm(1l);
        verify(jvmInfoService, atLeastOnce()).deleteJvm(1l);
    }

}
