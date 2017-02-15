package com.cerner.jwala.web.javascript.variable.property;

import org.junit.Test;

import static com.cerner.jwala.web.javascript.variable.property.ApplicationPropertySourceDefinition.LOAD_BALANCER_STATUS_MOUNT;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link ApplicationPropertySourceDefinition}
 * Created by Jedd Cuison on 2/15/2017
 */
public class ApplicationPropertySourceDefinitionTest {

    @Test
    public void testLoadBalancerStatusMount() {
        assertEquals("/balancer-manager", LOAD_BALANCER_STATUS_MOUNT.getDefaultValue());
        assertEquals("mod_jk.load-balancer.status.mount", LOAD_BALANCER_STATUS_MOUNT.getPropertyKey());
        assertEquals("loadBalancerStatusMount", LOAD_BALANCER_STATUS_MOUNT.getVariableName());
        assertEquals("com.cerner.jwala.web.javascript.variable.property.ApplicationPropertySourceDefinition@28f67ac7[variableName=loadBalancerStatusMount,propertyKey=mod_jk.load-balancer.status.mount,defaultValue=/balancer-manager]", LOAD_BALANCER_STATUS_MOUNT.toString());
    }

}
