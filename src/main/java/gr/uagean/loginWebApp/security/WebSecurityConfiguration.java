/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.security;

import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

/**
 *
 * @author nikos
 */
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .anyRequest()
//                .fullyAuthenticated()

//        http.oauth2Login()
//                .loginPage("/eidas-idp/oauth_login")
//                .authorizationEndpoint()
//                .baseUri("/eidas-idp/oauth_login/authorization");
        http.authorizeRequests()
                //
                .antMatchers("/").hasAuthority("offline_access")
                .antMatchers("/as/protected").hasAuthority("offline_access")
                .antMatchers("/is/protected").hasAuthority("offline_access")
                .antMatchers("/protected").hasAuthority("offline_access")
                .antMatchers("/eidas-idp/as/protected").hasAuthority("offline_access")
                .antMatchers("/eidas-idp/protected/protected").hasAuthority("offline_access")
                .antMatchers("/eidas-idp/is/protected").hasAuthority("offline_access")
                .antMatchers("/**").permitAll()
                .and()
                .oauth2Client()
                .and()
                .oauth2Login()
                //                .successHandler(new RefererAuthenticationSuccessHandler())

                .userInfoEndpoint()
                .userAuthoritiesMapper(userAuthoritiesMapper());

    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(
                    authority -> {
                        if (authority instanceof OidcUserAuthority) {
                            OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;

                            OidcIdToken idToken = oidcUserAuthority.getIdToken();
                            OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

//                            List<SimpleGrantedAuthority> groupAuthorities
//                            = userInfo.getClaimAsStringList("groups").stream()
//                                    .map(g -> new SimpleGrantedAuthority("ROLE_" + g.toUpperCase()))
//                                    .collect(Collectors.toList());
//                            SimpleGrantedAuthority auth = ;
                            mappedAuthorities.add(new SimpleGrantedAuthority("offline_access"));
                        }
                    });

            return mappedAuthorities;
        };
    }

}
