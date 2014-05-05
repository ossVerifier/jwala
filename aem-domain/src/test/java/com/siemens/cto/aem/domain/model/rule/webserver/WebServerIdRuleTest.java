package com.siemens.cto.aem.domain.model.rule.webserver;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.AbstractIdRuleTest;
import com.siemens.cto.aem.domain.model.rule.Rule;
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
