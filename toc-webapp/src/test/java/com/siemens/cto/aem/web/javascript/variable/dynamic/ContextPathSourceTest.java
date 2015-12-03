package com.siemens.cto.aem.web.javascript.variable.dynamic;

import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariable;
import com.siemens.cto.aem.web.javascript.variable.StringJavaScriptVariable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContextPathSourceTest {

    private ContextPathSource source;

    @Mock
    private ServletContext context;

    @Before
    public void setup() throws Exception {
        source = new ContextPathSource(context);
    }

    @Test
    public void testCreateSingleVariable() throws Exception {
        final String expectedContextPath = "myExpectedContextPathValue";
        final StringJavaScriptVariable expectedVariable = new StringJavaScriptVariable("contextPath",
                                                                                       expectedContextPath);

        when(context.getContextPath()).thenReturn(expectedContextPath);

        final JavaScriptVariable actualVariable = source.createSingleVariable();

        assertEquals(expectedVariable,
                     actualVariable);
    }
}
