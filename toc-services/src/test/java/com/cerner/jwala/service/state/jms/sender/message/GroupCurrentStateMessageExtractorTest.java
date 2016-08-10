package com.cerner.jwala.service.state.jms.sender.message;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupState;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.OperationalState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.service.state.jms.sender.message.GroupCurrentStateMessageExtractor;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupCurrentStateMessageExtractorTest extends AbstractCurrentStateMessageExtractorTest {

    private GroupCurrentStateMessageExtractor extractor;

    @Before
    public void setup() throws Exception {
        extractor = new GroupCurrentStateMessageExtractor();
    }

    @Test
    public void testExtract() throws Exception {
        final CurrentState expectedState = new CurrentState<>(new Identifier<Group>(123456L),
                                                              GroupState.GRP_STARTED,
                                                              DateTime.now(),
                                                              StateType.GROUP);

        setupMockMapMessage(expectedState);

        final CurrentState actualState = extractor.extract(message);
        assertEquals(expectedState,
                     actualState);
    }

    @Test
    public void testExtractUnknownState() throws JMSException {
        final OperationalState fakeState = mock(OperationalState.class);
        when(fakeState.toStateLabel()).thenReturn("This isn't a real Group State");

        final CurrentState expectedState = new CurrentState<>(new Identifier<Group>(123456L),
                                                              fakeState,
                                                              DateTime.now(),
                                                              StateType.GROUP);
        setupMockMapMessage(expectedState);
        final CurrentState actualState = extractor.extract(message);
        assertEquals(GroupState.GRP_UNKNOWN,
                     actualState.getState());
    }
}
