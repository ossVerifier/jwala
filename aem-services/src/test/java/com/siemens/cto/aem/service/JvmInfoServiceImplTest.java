package com.siemens.cto.aem.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.persistence.dao.GroupDaoJpa;
import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.persistence.domain.JpaGroup;
import com.siemens.cto.aem.persistence.domain.JpaJvm;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotDeletedException;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by z003bpej on 2/21/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class JvmInfoServiceImplTest {

    @Mock
    private GroupDaoJpa groupDaoJpa;

    @Mock
    private JvmDaoJpa jvmDaoJpa;

    private JvmInfoService jvmInfoService;

    @Mock
    private JpaJvm jvm;

    @Before
    public void setUp() {
        when(jvm.getId()).thenReturn(1L);
        when(jvm.getName()).thenReturn("the-jvm-name");
        when(jvm.getHostName()).thenReturn("the-jvm-hostname");
        when(jvm.getGroup()).thenReturn(new JpaGroup());

        jvmInfoService = new JvmInfoServiceImpl(groupDaoJpa, jvmDaoJpa);
    }

    @Test
    public void testGetJvmInfoById() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        final JvmInfo jvmInfo = jvmInfoService.getJvmInfoById(1L);
        assertEquals(new Long(1), jvmInfo.getId());
        assertEquals("the-jvm-name", jvmInfo.getName());
        assertEquals("the-jvm-hostname", jvmInfo.getHost());
    }

    @Test
    public void testGetAllJvmInfo() {
        final List<JpaJvm> jvmList = new ArrayList<JpaJvm>();
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
        jvmInfoService.addJvmInfo("the-jvm-name", "the-host-name", new GroupInfo("the-test-group"));
        verify(jvmDaoJpa, times(1)).add(any(JpaJvm.class));
    }

    @Test
    @Ignore
    public void testAddJvmInfoWithGroup() {
        // TODO: Implement the test
        throw new UnsupportedOperationException();
    }

    @Test(expected = RecordNotAddedException.class)
    public void testFailureToAddJvmInfo() {
        doThrow(EntityExistsException.class).when(jvmDaoJpa).add(any(JpaJvm.class));
        jvmInfoService.addJvmInfo("the-jvm-name", "the-host-name", new GroupInfo("the-test-group"));
        verify(jvmDaoJpa, times(1)).add(any(JpaJvm.class));
    }

    @Test
    public void testUpdateJvmInfo() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        jvmInfoService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvm, times(1)).setName(anyString());
        verify(jvm, times(1)).setHostName(anyString());
    }

    @Test
    @Ignore
    public void testUpdateJvmInfoAndGroupName() {
        // TODO: Implement the test
        throw new UnsupportedOperationException();
    }

    @Test(expected = RecordNotFoundException.class)
    public void testUpdateJvmInfoThatDoesNotExist() {
        doThrow(Exception.class).when(jvmDaoJpa).findById(null);
        jvmInfoService.updateJvmInfo(1l, "the-jvm-name", "the-host-name");
        verify(jvmDaoJpa, times(0)).update(any(JpaJvm.class));
    }

    @Test
    public void testDeleteJvm() {
        when(jvmDaoJpa.findById(eq(new Long(1)))).thenReturn(jvm);
        jvmInfoService.deleteJvm(1l);
        verify(jvmDaoJpa, times(1)).remove(any(JpaJvm.class));
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
