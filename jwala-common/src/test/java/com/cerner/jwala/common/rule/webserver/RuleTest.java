package com.cerner.jwala.common.rule.webserver;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.path.FileSystemPath;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.rule.*;
import com.cerner.jwala.common.rule.app.ApplicationIdRule;
import com.cerner.jwala.common.rule.app.GoodStreamRule;
import com.cerner.jwala.common.rule.group.GroupIdRule;
import com.cerner.jwala.common.rule.group.GroupIdsRule;
import com.cerner.jwala.common.rule.group.GroupNameRule;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;
import com.cerner.jwala.common.rule.jvm.JvmIdsRule;
import com.cerner.jwala.common.rule.jvm.JvmStateRule;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Generic test for classes that implement {@link Rule}.
 *
 * Created by Jedd Cuison on 2/16/2016.
 */
public class RuleTest {
    private Path mockPath = mock(Path.class);
    private Identifier [] groupIds = {new Identifier<Group>(1L)};
    private Identifier [] jvmIds = {new Identifier<Jvm>(1L)};
    private Rule [] rules = {new HostNameRule("any"),
                             new MapNotEmptyRule(mock(Map.class)),
                             new MultipleRules(),
                             new PathRule(mockPath),
                             new PortNumberRule(8080, AemFaultType.CANNOT_CONNECT),
                             new ShutdownPortNumberRule(8080, AemFaultType.CANNOT_CONNECT),
                             new StatusPathRule(mockPath),
                             new StringLengthRule(1, 10, "any"),
                             new ValidTemplateNameRule("any"),
                             new ValidWebArchiveNameRule("any"),
                             new ApplicationIdRule(new Identifier<Application>(1L)),
                             new GoodStreamRule(mock(InputStream.class)),
                             new GroupIdRule(groupIds[0]),
                             new GroupIdsRule(new HashSet<Identifier<Group>>(Arrays.<Identifier<Group>>asList(groupIds))),
                             new GroupNameRule("any"),
                             new JvmIdRule(jvmIds[0]),
                             new JvmIdsRule(new HashSet<Identifier<Jvm>>(Arrays.<Identifier<Jvm>>asList(jvmIds))),
                             new JvmStateRule(JvmState.JVM_STOPPED),
                             new HttpConfigFileRule(mock(FileSystemPath.class)),
                             new WebServerIdRule(new Identifier<WebServer>(1L)),
                             new WebServerReachableStateRule(WebServerReachableState.WS_UNREACHABLE)};

    @Before
    public void setup() {
        when(mockPath.isAbsolute()).thenReturn(true);
        when(mockPath.getPath()).thenReturn("any");
    }

    @Test
    public void ruleTest() {
        for (Rule rule : rules) {
            try {
                rule.isValid();
                rule.validate();
            } catch (final RuntimeException e) {
                if (rule.getClass().getSimpleName().equalsIgnoreCase("StatusPathRule") &&
                    !e.getClass().getSimpleName().equalsIgnoreCase("BadRequestException")) {
                    e.printStackTrace();
                    fail("There shouldn't be any exceptions thrown for this test to pass! Class = " + rule.getClass().getSimpleName() +
                            "; Exception Message = " + e.getMessage());
                }
            }
        }
    }

}
