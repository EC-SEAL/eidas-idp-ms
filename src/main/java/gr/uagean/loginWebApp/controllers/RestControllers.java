/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.model.pojo.EntityMetadata;
import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import gr.uagean.loginWebApp.service.EsmoMetadataService;
import gr.uagean.loginWebApp.service.HttpSignatureService;
import gr.uagean.loginWebApp.service.KeyStoreService;
import gr.uagean.loginWebApp.service.MSConfigurationService;
import gr.uagean.loginWebApp.service.NetworkService;
import gr.uagean.loginWebApp.service.ParameterService;
import gr.uagean.loginWebApp.service.impl.HttpSignatureServiceImpl;
import gr.uagean.loginWebApp.service.impl.NetworkServiceImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author nikos
 */
@Controller
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST})
public class RestControllers {

    @Autowired
    private ParameterService paramServ;

    private NetworkService netServ;
    private KeyStoreService keyServ;

    @Autowired
    private EsmoMetadataService metadataServ;

    @Autowired
    private MSConfigurationService configServ;

    private final static Logger Log = LoggerFactory.getLogger(RestControllers.class);

    final static String CLIENT_ID = "CLIENT_ID";
    final static String REDIRECT_URI = "REDIRECT_URI";
    final static String HTTP_HEADER = "HTTP_HEADER";
    final static String URL_ENCODED = "URL_ENCODED";
    final static String URL_PREFIX = "URL_PREFIX";

    @Autowired
    public RestControllers(KeyStoreService keyServ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, InvalidKeySpecException, IOException {
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getHttpSigningKey();
        String fingerPrint = DigestUtils.sha256Hex(this.keyServ.getHttpSigPublicKey().getEncoded());
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(this.keyServ);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = {"/as/authenticate", "/is/query", "/eidas-idp/as/authenticate", "/eidas-idp/is/query"},
            method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView authenticate(@RequestParam String msToken,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttrs, Model model
    ) {

        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.FOUND);
        ObjectMapper mapper = new ObjectMapper();
        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        List<NameValuePair> getParams = new ArrayList();
        getParams.add(new NameValuePair("token", msToken));

        Log.info("got the token:" + msToken);
        try {
            //calls SM, get /sm/validateToken, to validate the received token and get the sessionId
            SessionMngrResponse resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/validateToken", getParams, 1), SessionMngrResponse.class);
            Log.info("got the responseCode:" + resp.getCode().toString());
            if (resp.getCode().toString().equals("OK")) {
                String sessionId = resp.getSessionData().getSessionId();
                Log.info("will continue to redirect view:: sessionId" + sessionId);
                getParams.clear();
                getParams.add(new NameValuePair("sessionId", sessionId));
                // get the session data that are stored on the session manager
//                resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", getParams, 1), SessionMngrResponse.class);
//                String spOrigin = (String) resp.getSessionData().getSessionVariables().get("SP_ORIGIN");
//                Log.info("got SP origin:" + spOrigin);

                // redirect to a protected view, passing esmoSessionId as a parameter
                // next this parameter will will be passed to the OIDC server as state
                // and will picked up on response, so we can continue with the ESMO session
                model.addAttribute("login", "protected"); // the idp ms url to redirect to in the protectedRedirectForm
                model.addAttribute("sessionId", sessionId); // the sessionId that should be passed as state

                Cookie cookie = new Cookie("seal", sessionId);
                cookie.setMaxAge(5 * 60); // expires in 7 days
//                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                cookie.setPath("/"); // global cookie accessible every where
                response.addCookie(cookie);
                return new ModelAndView("protectedRedirect");

            }
        } catch (Exception e) {
            Log.error(e.getMessage());
        }

        return null;
    }

    @RequestMapping(value = "/meta", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody
    EntityMetadata getMetadata() throws IOException, KeyStoreException {
        EntityMetadata metadata = this.metadataServ.getMetadata();
        return metadata;
    }

}
