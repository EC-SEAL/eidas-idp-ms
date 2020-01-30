/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.security;

import gr.uagean.loginWebApp.model.enums.RolesEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 * @author nikos
 */
public class ResourceAwareAuthenticationProvider extends KeycloakAuthenticationProvider {

    private boolean convertToUpperCase = false;
    private boolean convertToLowerCase = false;
    private String prefix = "ROLE_";

    public ResourceAwareAuthenticationProvider() {
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) super.authenticate(authentication);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

        for (String role : token.getAccount().getRoles()) {
            grantedAuthorities.add(new KeycloakRole(role));
        }

//        Optional<User> user = userServ.findByEid(token.getAccount().getKeycloakSecurityContext().getIdToken().getPreferredUsername());
//        if (user.isPresent()) {
//            grantedAuthorities.add(new KeycloakRole(user.get().getRole().getName()));
//        }
        Authentication result = new KeycloakAuthenticationToken(token.getAccount(), token.isInteractive(), mapAuthorities(grantedAuthorities));
        result.setAuthenticated(token.isAuthenticated());

        return result;
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        HashSet<GrantedAuthority> mapped = new HashSet<>(
                authorities.size());
        for (GrantedAuthority authority : authorities) {
            mapped.add(mapAuthority(authority.getAuthority()));
        }

        return mapped;
    }

    private GrantedAuthority mapAuthority(String name) {
        if (convertToUpperCase) {
            name = name.toUpperCase();
        } else if (convertToLowerCase) {
            name = name.toLowerCase();
        }

        if (prefix.length() > 0 && !name.startsWith(prefix)) {
            name = prefix + name;
        }
        return new SimpleGrantedAuthority(name);
    }
}

class CustomAuthority implements GrantedAuthority {

    @Override
    public String getAuthority() {
        return RolesEnum.ADMIN.role();
    }
}
