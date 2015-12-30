package com.siemens.cto.aem.service.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
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
    private HistoryCrudService mockHistoryCrudService;

    private HistoryService historyService;

    @Before
    public void setUp() {
        initMocks(this);
        historyService = new HistoryServiceImpl(mockHistoryCrudService);
    }

    @Test
    public void testWrite() {
        final List<Group> groups = new ArrayList<>();
        groups.add(new Group(Identifier.<Group>id(1L), "testGroup"));
        historyService.createHistory("any", groups, "Testing...", EventType.USER_ACTION, "user");
        verify(mockHistoryCrudService).createHistory(eq("any"), any(Group.class), eq("Testing..."),
                eq(EventType.USER_ACTION), eq("user"));
    }

    @Test
    public void testRead() {
        historyService.findHistory("any", "any", null);
        verify(mockHistoryCrudService).findHistory(eq("any"), eq("any"), anyInt());
    }

}
