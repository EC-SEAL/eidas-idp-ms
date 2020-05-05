/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import gr.uagean.loginWebApp.service.CountryService;
import gr.uagean.loginWebApp.service.EidasPropertiesService;
import gr.uagean.loginWebApp.service.ParameterService;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.StringUtils;

/**
 *
 * @author nikos
 */
@Controller
public class ViewControllers {

    final static String EIDAS_URL = "EIDAS_NODE_URL";
    final static String SP_FAIL_PAGE = "SP_FAIL_PAGE";
    final static String SP_SUCCESS_PAGE = "SP_SUCCESS_PAGE";
    final static String SP_LOGO = System.getenv("SP_LOGO");

    final static String UAEGEAN_LOGIN = "UAEGEAN_LOGIN";
    final static String LINKED_IN_SECRET = "LINKED_IN_SECRET";
    final static String SECRET = "SP_SECRET";

    final static String CLIENT_ID = "CLIENT_ID";
    final static String REDIRECT_URI = "REDIRECT_URI";
    final static String HTTP_HEADER = "HTTP_HEADER";
    final static String URL_ENCODED = "URL_ENCODED";
    final static String URL_PREFIX = "URL_PREFIX";

    @Value("${eidas.error.consent}")
    private String EIDAS_CONSENT_ERROR;
    @Value("${eidas.error.qaa}")
    private String EIDAS_QAA_ERROR;
    @Value("${eidas.error.missing.gr}")
    private String EIDAS_MISSING_ATTRIBUTE_ERROR_GR;

    final static Logger LOG = LoggerFactory.getLogger(ViewControllers.class);

    @Autowired
    private EidasPropertiesService propServ;

    @Autowired
    private CountryService countryServ;

    @Autowired
    private ParameterService paramServ;

    @RequestMapping(value = {"/login", "/idp/login", "gr/idp/login", "/eidas-idp/login", "/eidas-idp/idp/login", "/eidas-idp/gr/idp/login"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView loginView(HttpServletRequest request, @RequestParam(value = "sessionId", required = true) String idpMsSession) {

        ModelAndView mv = new ModelAndView("login");
//        mv.addObject("nodeUrl", SpEidasSamlTools.getNodeUrl());
        mv.addObject("nodeUrl", this.paramServ.getParam("EIDAS_NODE_URL"));
        mv.addObject("countries", countryServ.getEnabled());
        mv.addObject("spFailPage", System.getenv(SP_FAIL_PAGE));
        mv.addObject("spSuccessPage", System.getenv(SP_SUCCESS_PAGE));
        mv.addObject("logo", SP_LOGO);

        mv.addObject("legal", propServ.getLegalProperties());
        mv.addObject("natural", propServ.getNaturalProperties());
        String urlPrefix = StringUtils.isEmpty(paramServ.getParam(URL_PREFIX)) ? "" : paramServ.getParam(URL_PREFIX);

        String clientID = paramServ.getParam(CLIENT_ID);
        String redirectURI = paramServ.getParam(REDIRECT_URI);
        String responseType = "code";
        String state = UUID.randomUUID().toString();
        mv.addObject("clientID", clientID);
        mv.addObject("redirectURI", redirectURI);
        mv.addObject("responseType", responseType);
        mv.addObject("state", state);
        mv.addObject("linkedIn", false);
        mv.addObject("uAegeanLogin", false);

        mv.addObject("urlPrefix", urlPrefix);

        mv.addObject("idpMsSession", idpMsSession);

        // TODO hide stuff that are not related to esmo from the view
        return mv;
    }

    @RequestMapping({"/authfail", "/eidas-idp/authfail"})
    public String authorizationFail(@RequestParam(value = "t", required = false) String token,
            @RequestParam(value = "reason", required = false) String reason,
            @CookieValue(value = "localeInfo", required = false) String langCookie,
            Model model) {

        if (reason != null) {
            model.addAttribute("title", "Registration/Login Cancelled");
            switch (reason) {
                case "disagree":
                    model.addAttribute("title", "Registration/Login termintated");
                    model.addAttribute("errorType", "DISAGREE");
                    break;
                case "consent":
                    model.addAttribute("errorMsg", EIDAS_CONSENT_ERROR);
                    break;
                case "qa":
                    model.addAttribute("errorMsg", EIDAS_QAA_ERROR);
                    break;
                case "attr":
                    model.addAttribute("errorMsg", EIDAS_MISSING_ATTRIBUTE_ERROR_GR);
                    break;
                case "fail":
                    model.addAttribute("title", "Non-sucessful authentication");
                    model.addAttribute("errorMsg", "Please, return to the home page and re-initialize the process. If the authentication fails again, please contact your national eID provider");
                    break;
                case "esmo":
                    model.addAttribute("title", "Non-sucessful authentication");
                    break;
                default:
                    model.addAttribute("errorType", "CANCEL");
                    break;
            }
        } else {
            model.addAttribute("title", "");
            if (langCookie != null && langCookie.equals("gr")) {
                model.addAttribute("errorMsg", "Η διαδικασία ταυτοποίησης ακυρώθηκε από το χρήστη");

            } else {
                model.addAttribute("errorMsg", "User Canceled Authentication process");

            }

        }

        String urlPrefix = StringUtils.isEmpty(paramServ.getParam(URL_PREFIX)) ? "" : paramServ.getParam(URL_PREFIX);
        model.addAttribute("urlPrefix", urlPrefix);

        model.addAttribute("server", System.getenv("SP_SERVER"));
        model.addAttribute("logo", SP_LOGO);

        return "authfail";
    }

}
