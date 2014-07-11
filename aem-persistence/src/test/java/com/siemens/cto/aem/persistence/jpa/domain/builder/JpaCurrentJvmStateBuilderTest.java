package com.siemens.cto.aem.persistence.jpa.domain.builder;

import java.util.Locale;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentJvmState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JpaCurrentJvmStateBuilderTest {

    private JpaCurrentJvmStateBuilder builder;
    private JpaCurrentJvmState jpaState;

    @Before
    public void setUp() throws Exception {
        jpaState = mock(JpaCurrentJvmState.class);
        builder = new JpaCurrentJvmStateBuilder(jpaState);
    }

    @Test
    public void testBuild() throws Exception {
        final CurrentJvmState expectedCurrentState = new CurrentJvmState(new Identifier<Jvm>(123456L),
                                                                         JvmState.STARTED,
                                                                         DateTime.now());

        when(jpaState.getId()).thenReturn(expectedCurrentState.getJvmId().getId());
        when(jpaState.getState()).thenReturn(expectedCurrentState.getJvmState().toStateString());
        when(jpaState.getAsOf()).thenReturn(expectedCurrentState.getAsOf().toCalendar(Locale.getDefault()));

        final CurrentJvmState actualCurrentState = builder.build();

        assertEquals(expectedCurrentState,
                     actualCurrentState);
    }

    @Test
    public void testBuildNull() throws Exception {
        builder = new JpaCurrentJvmStateBuilder();
        builder.setJvmState(null);

        final CurrentJvmState actualState = builder.build();

        assertNull(actualState);
    }
}
