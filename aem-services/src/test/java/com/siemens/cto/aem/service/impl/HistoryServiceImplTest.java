package com.siemens.cto.aem.service.impl;

import com.siemens.cto.aem.persistence.dao.HistoryDao;
import com.siemens.cto.aem.service.HistoryService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

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
        historyService.write(null, "Testing...");
        verify(mockHistoryDao).write(null, "Testing...");
    }
}
