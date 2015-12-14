package com.siemens.cto.aem.common.rule.webserver;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.rule.AbstractIdRuleTest;
import com.siemens.cto.aem.common.rule.Rule;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

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
