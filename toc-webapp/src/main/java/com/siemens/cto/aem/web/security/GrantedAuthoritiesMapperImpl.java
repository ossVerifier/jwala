package com.siemens.cto.aem.web.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.stereotype.Component;

import com.siemens.cto.aem.common.properties.ApplicationProperties;

/**
 * @author Cerner
 *
 */
@Component
public class GrantedAuthoritiesMapperImpl implements GrantedAuthoritiesMapper {
    private static final String PROP_TOC_ROLE_USER = "toc.role.user";
    private static final String PROP_TOC_ROLE_ADMIN = "toc.role.admin";
    public final static String TOC_ROLE_USER = ApplicationProperties.get(PROP_TOC_ROLE_USER);
    public final static String TOC_ROLE_ADMIN = ApplicationProperties.get(PROP_TOC_ROLE_ADMIN);

    /* (non-Javadoc)
     * @see org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper#mapAuthorities(java.util.Collection)
     */
    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
        //Add only TOC groups as authorities
        for (GrantedAuthority a : authorities) {
            if (TOC_ROLE_USER.equals(a.getAuthority())) {
                roles.add(new SimpleGrantedAuthority(TOC_ROLE_USER));
            } else if (TOC_ROLE_ADMIN.equals(a.getAuthority())) {
                roles.add(new SimpleGrantedAuthority(TOC_ROLE_ADMIN));
            }
        }
        return roles;
    }   
}
