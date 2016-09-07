package com.cerner.jwala.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.web.security.GrantedAuthoritiesMapperImpl;

public class GrantedAuthoritiesMapperTests {
    GrantedAuthoritiesMapperImpl authorities;
    
    SimpleGrantedAuthority user;
    SimpleGrantedAuthority admin;
    Collection<SimpleGrantedAuthority> auths;


    @Before
    public void setUp() throws Exception {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        authorities = new GrantedAuthoritiesMapperImpl();
        auths = new HashSet<SimpleGrantedAuthority>();
        user = new SimpleGrantedAuthority(ApplicationProperties.get("toc.role.user"));
        admin = new SimpleGrantedAuthority(ApplicationProperties.get("toc.role.admin"));
        auths.add(user);
        auths.add(admin);
    }

    @Test
    public void testMapAuthorities() {
            assertNotNull(authorities.mapAuthorities(auths));
            assertEquals(1, authorities.mapAuthorities(auths).size());
     }

}
