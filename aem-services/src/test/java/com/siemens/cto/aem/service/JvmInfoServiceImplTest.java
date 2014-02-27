package com.siemens.cto.aem.service;

import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.persistence.domain.Jvm;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.exception.RecordNotUpdatedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
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
public class JvmInfoServiceImplTest {

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

    @Test(expected = RecordNotAddedException.class)
    public void testFailureToAddJvmInfo() {
        doThrow(EntityExistsException.class).when(jvmDaoJpa).add(any(Jvm.class));
        jvmInfoService.addJvmInfo("the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(1)).add(any(Jvm.class));
    }

    @Test
    public void testUpdateJvmInfo() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        jvmInfoService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(1)).update(any(Jvm.class));
    }

    @Test(expected = RecordNotUpdatedException.class)
    public void testFailureToUpdateJvmInfoSinceEntityDoesNotExist() {
        doThrow(Exception.class).when(jvmDaoJpa).findById(eq(new Long(1)));
        jvmInfoService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(0)).update(any(Jvm.class));
    }

    @Test(expected = RecordNotUpdatedException.class)
    public void testFailureToUpdateJvmInfoDueToMergingError() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        doThrow(PersistenceException.class).when(jvmDaoJpa).update(any(Jvm.class));
        jvmInfoService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(1)).update(any(Jvm.class));
    }

    @Test
    public void testDeleteJvm() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        jvmInfoService.deleteJvm(1l);
        verify(jvmDaoJpa, times(1)).remove(any(Jvm.class));
    }

    @Test(expected = RecordNotFoundException.class)
    public void testDeleteJvmThatDoesNotExist() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(null);
        jvmInfoService.deleteJvm(1l);
    }

    @Test(expected = RecordNotDeletedException.class)
    public void testFailureToDeleteJvm() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        doThrow(RecordNotDeletedException.class).when(jvmDaoJpa).remove(jvm);
        jvmInfoService.deleteJvm(1l);
    }

}
