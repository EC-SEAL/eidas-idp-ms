/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.security;

import gr.uagean.loginWebApp.model.enums.RolesEnum;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 *
 * @author nikos
 */
@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        ResourceAwareAuthenticationProvider keycloakAuthenticationProvider = new ResourceAwareAuthenticationProvider();
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    public KeycloakConfigResolver KeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    /**
     * Defines the session authentication strategy.
     */
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        super.configure(http);

        String[] extendedRoles = new String[]{RolesEnum.ADMIN.role(),
            RolesEnum.SUPERADMIN.role(), RolesEnum.COORDINATOR.role(),
            RolesEnum.VIRTUALPARTICIPANT.role(), RolesEnum.VISITOR.role()};

        String[] adminRoles = new String[]{RolesEnum.ADMIN.role(),
            RolesEnum.SUPERADMIN.role()};

        http.authorizeRequests().antMatchers("/").hasRole("offline_access")
                .antMatchers("/as/protected").hasRole("offline_access")
                .and()
                .exceptionHandling()
                .accessDeniedPage("/error")
                .and()
                .logout()
                .logoutUrl("/logout")
                .deleteCookies("access_token")
                .logoutSuccessUrl("/")
                .and()
                .csrf().disable();

//        http.authorizeRequests().antMatchers("/login")
//                .hasRole("offline_access");
//
//        http.authorizeRequests()
//                .antMatchers("/getUsersByRole**").hasAnyRole(extendedRoles)
//                .and().formLogin()
//                .loginPage("/sso/login").permitAll();
//        http
//                .authorizeRequests()
//                .antMatchers("/updateclass").hasAnyRole(extendedRoles)
//                .antMatchers("/team").hasAnyRole(extendedRoles)
//                .antMatchers("/updateUserRole**").hasAnyRole(extendedRoles)
//                .and().formLogin()
//                .loginPage("/landing").permitAll()
//                .and()
//                .exceptionHandling()
//                .accessDeniedPage("/error")
//                .and()
//                .logout()
//                .logoutUrl("/logout")
//                .deleteCookies("access_token")
//                .logoutSuccessUrl("/") //logoutSuccessUrl("/login?logout")
//                .and()
//                .csrf().disable();
//
//        http.authorizeRequests()
//                .antMatchers("/admin**").
//                hasAnyRole(adminRoles)
//                .antMatchers("/admin/edit**").hasAnyRole(adminRoles)
//                .antMatchers("/addSkypeRoom**").hasAnyRole(adminRoles)
//                .antMatchers("/deleteSkypeRoom**").hasAnyRole(adminRoles);
    }

}
