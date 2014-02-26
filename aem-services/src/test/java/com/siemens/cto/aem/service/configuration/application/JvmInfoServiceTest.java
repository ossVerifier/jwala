package com.siemens.cto.aem.service.configuration.application;

import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.persistence.domain.Jvm;
import com.siemens.cto.aem.service.JvmInfo;
import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.service.JvmInfoServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by z003bpej on 2/21/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class JvmInfoServiceTest {

    @Mock
    private JvmDaoJpa jvmDaoJpa;

    private JvmInfoService jvmInfoService;

    @Mock
    private Jvm jvm;

    @Before
    public void setUp() {
        when(jvm.getId()).thenReturn(new Long(1));
        when(jvm.getName()).thenReturn("the-jvm-name");
        when(jvm.getHostName()).thenReturn("the-jvm-hostname");
        jvmInfoService = new JvmInfoServiceImpl(jvmDaoJpa);
    }

    @Test
    public void testGetJvmInfoById() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        final JvmInfo jvmInfo = jvmInfoService.getJvmInfoById(new Long(1));
        assertEquals(new Long(1), jvmInfo.getId());
        assertEquals("the-jvm-name", jvmInfo.getName());
        assertEquals("the-jvm-hostname", jvmInfo.getHost());
    }

    @Test
    public void testGetAllJvmInfo() {
        final List<Jvm> jvmList = new ArrayList<Jvm>();
        jvmList.add(jvm);
        when(jvmDaoJpa.findAll()).thenReturn(jvmList);
        final List<JvmInfo> jvmInfoList = jvmInfoService.getAllJvmInfo();
        assertEquals(1, jvmInfoList.size());
        assertEquals(new Long(1), jvmInfoList.get(0).getId());
        assertEquals("the-jvm-name", jvmInfoList.get(0).getName());
        assertEquals("the-jvm-hostname", jvmInfoList.get(0).getHost());
    }

    @Test
    public void testAddJvmInfo() {
        jvmInfoService.addJvmInfo("the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(1)).add(any(Jvm.class));
    }

    @Test
    public void testUpdateJvmInfo() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        jvmInfoService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(1)).update(any(Jvm.class));
    }

    @Test
    public void testDeleteJvm() {
        jvmInfoService.deleteJvm(1l);
        verify(jvmDaoJpa, times(1)).remove(any(Jvm.class));
    }

}
