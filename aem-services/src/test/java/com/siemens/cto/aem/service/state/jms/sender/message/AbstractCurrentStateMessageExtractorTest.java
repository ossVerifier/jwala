package com.siemens.cto.aem.service.state.jms.sender.message;

import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.domain.model.state.message.StateKey;
import org.joda.time.format.ISODateTimeFormat;
import org.mockito.Mock;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class AbstractCurrentStateMessageExtractorTest {

    @Mock
    protected MapMessage message;

    protected <S, T extends OperationalState> void setupMockMapMessage(final CurrentState<S, T> aState) throws JMSException {
        mockMapString(CommonStateKey.AS_OF, ISODateTimeFormat.dateTime().print(aState.getAsOf()));
        mockMapString(CommonStateKey.ID, aState.getId().getId().toString());
        mockMapString(CommonStateKey.STATE, aState.getState().toPersistentString());
        mockMapString(CommonStateKey.TYPE, aState.getType().name());
        mockMapString(CommonStateKey.MESSAGE, aState.getMessage());
    }

    private void mockMapString(final StateKey aKey,
                               final String aValue) throws JMSException {
        when(message.getString(eq(aKey.getKey()))).thenReturn(aValue);
    }
}
