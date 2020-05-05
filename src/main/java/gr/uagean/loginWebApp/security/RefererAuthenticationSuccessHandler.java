/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

/**
 *
 * @author nikos
 */
public class RefererAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    public RefererAuthenticationSuccessHandler() {
        setUseReferer(true);   // use referer
        //  setTargetUrlParameter(REDIRECT_PARAMETER);   // if you want to use query param
    }

//    @Override
//    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
//        //   you should check if the redirect parameter is missing or not in url format.
//        return (String) request.getParameter("redirect");
//    }
}
