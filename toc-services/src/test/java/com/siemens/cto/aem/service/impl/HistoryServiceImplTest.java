package com.siemens.cto.aem.service.impl;

import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link HistoryServiceImpl}
 *
 * Created by JC043760 on 12/2/2015.
 */
public class HistoryServiceImplTest {

    @Mock
    private HistoryDao mockHistoryDao;

    private HistoryService historyService;

    @Before
    public void setUp() {
        initMocks(this);
        historyService = new HistoryServiceImpl(mockHistoryDao);
    }

    @Test
    public void testWrite() {
        final List<JpaGroup> groups = new ArrayList<>();
        groups.add(new JpaGroup());
        historyService.createHistory("any", groups, "Testing...", EventType.USER_ACTION, "user");
        verify(mockHistoryDao).createHistory(eq("any"), any(JpaGroup.class), eq("Testing..."),
                eq(EventType.USER_ACTION), eq("user"));
    }

    @Test
    public void testRead() {
        historyService.findHistory("any", null);
        verify(mockHistoryDao).findHistory(eq("any"), anyInt());
    }

}
