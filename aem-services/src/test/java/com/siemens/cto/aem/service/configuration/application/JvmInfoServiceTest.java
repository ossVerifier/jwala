package com.siemens.cto.aem.service.configuration.application;

import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.persistence.domain.Jvm;
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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by z003bpej on 2/21/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class JvmInfoServiceTest {

    @Mock
    private JvmDaoJpa jvmDaoJpa;

    @InjectMocks
    private JvmInfoService jvmInfoService = new JvmInfoServiceImpl();

    @Mock
    private Jvm jvm;

    @Before
    public void setUp() {
        when(jvm.getId()).thenReturn(new Long(1));
        when(jvm.getName()).thenReturn("the-jvm-name");
    }

    @Test
    public void testGetJvmInfoById() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        final Jvm jvm = jvmInfoService.getJvmInfoById(new Long(1));
        assertEquals(new Long(1), jvm.getId());
        assertEquals("the-jvm-name", jvm.getName());
    }

    @Test
    public void testGetAllJvmInfo() {
        final List<Jvm> jvmList = new ArrayList<Jvm>();
        jvmList.add(jvm);
        when(jvmDaoJpa.findAll()).thenReturn(jvmList);
        final List<Jvm> resultJvmList = jvmInfoService.getAllJvmInfo();
        assertEquals(1, resultJvmList.size());
        assertEquals(new Long(1), resultJvmList.get(0).getId());
        assertEquals("the-jvm-name", resultJvmList.get(0).getName());
    }

    @Test
    public void testAddJvmInfo() {
        jvmInfoService.addJvmInfo("the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(1)).add(any(Jvm.class));
    }

    @Test
    public void testUpdateJvmInfo() {
        jvmInfoService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(1)).update(any(Jvm.class));
    }

    @Test
    public void testDeleteJvm() {
        jvmInfoService.deleteJvm(1l);
        verify(jvmDaoJpa, times(1)).remove(any(Jvm.class));
    }

}
