package com.siemens.cto.aem.rule.webserver;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.rule.AbstractIdRuleTest;
import com.siemens.cto.aem.rule.Rule;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

public class WebServerIdRuleTest extends AbstractIdRuleTest {

    @Override
    protected Rule createValidRule() {
        return new WebServerIdRule(new Identifier<WebServer>(1L));
    }

    @Override
    protected Rule createInvalidRule() {
        return new WebServerIdRule(null);
    }
}
