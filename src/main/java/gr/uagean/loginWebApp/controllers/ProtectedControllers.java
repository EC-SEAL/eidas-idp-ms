/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.factory.AttributeSetFactory;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.DataSet;
import gr.uagean.loginWebApp.model.pojo.DataStore;
import gr.uagean.loginWebApp.model.pojo.EidasUser;
import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import gr.uagean.loginWebApp.model.pojo.UpdateDataRequest;
import gr.uagean.loginWebApp.service.EidasPropertiesService;
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
import java.security.Principal;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author nikos
 */
@Controller
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST})
public class ProtectedControllers {

    @Autowired
    private EidasPropertiesService propServ;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ParameterService paramServ;

    private NetworkService netServ;
    private KeyStoreService keyServ;

    @Autowired
    private EsmoMetadataService metadataServ;

    @Autowired
    private MSConfigurationService configServ;

    private final static Logger Log = LoggerFactory.getLogger(ProtectedControllers.class);

    final static String UAEGEAN_LOGIN = "UAEGEAN_LOGIN";
    final static String LINKED_IN_SECRET = "LINKED_IN_SECRET";

    final static String CLIENT_ID = "CLIENT_ID";
    final static String REDIRECT_URI = "REDIRECT_URI";
    final static String HTTP_HEADER = "HTTP_HEADER";
    final static String URL_ENCODED = "URL_ENCODED";
    final static String URL_PREFIX = "URL_PREFIX";

    @Autowired
    public ProtectedControllers(KeyStoreService keyServ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, InvalidKeySpecException, IOException {
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getHttpSigningKey();
        String fingerPrint = DigestUtils.sha256Hex(this.keyServ.getHttpSigPublicKey().getEncoded());
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(this.keyServ);
    }

    @RequestMapping(value = "/as/protected", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView authenticate(@RequestParam(value = "sessionId") String sessionId,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs, Model model, Principal principal
    ) {

        try {
            Log.info("Reached protected endpoint with sessionId" + sessionId);

            String eId = principal.getName();

            IDToken idToken = ((KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName())).getIdToken();

            EidasUser user = new EidasUser();
            user.setCurrentFamilyName(idToken.getFamilyName());
            user.setCurrentGivenName(idToken.getName());
            user.setDateOfBirth((String) idToken.getOtherClaims().get("date_of_birth"));
            user.setProfileName(idToken.getGivenName());
            user.setEid(eId);
            user.setPersonIdentifier((String) idToken.getOtherClaims().get("person_identifier"));

            String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
            List<NameValuePair> requestParams = new ArrayList();

            ObjectMapper mapper = new ObjectMapper();
            //IdP connector gets the datastore from the session manager and sets the eIDAS dataset object
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", sessionId));
            SessionMngrResponse resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", requestParams, 1), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("OK")) {
                Log.error("ERROR: " + resp.getError());
                return new ModelAndView("error");
            }
            String dataStoreString = (String) resp.getSessionData().getSessionVariables().get("dataStore");
            DataStore ds = new DataStore();
            List<DataSet> dsArrayList = new ArrayList();

            if (!StringUtils.isEmpty(dataStoreString)) {
                ds = mapper.readValue(dataStoreString, DataStore.class);
                DataSet[] OldDataSet = ds.getClearData();
                Arrays.stream(OldDataSet).forEach(dataSet -> {
                    dsArrayList.add(dataSet);
                });
            } else {
                String dsId = UUID.randomUUID().toString();
                ds.setId(dsId);
            }

            //IdP Connector updates the session with the variables received from the user authentication by calling the SM, with post, “/sm/updateSessionData”
            // to store the received attributes
            //create the dataset from the received attributes
            String id = UUID.randomUUID().toString();
            AttributeSet receivedAttributes = AttributeSetFactory.makeFromEidasResponse(id, TypeEnum.AUTHRESPONSE, "issuer", "recipient", user);
            DataSet dataSet = new DataSet();
            dataSet.setId(id);
            dataSet.setLoa(user.getLoa());
            dataSet.setIssued(id);
            dataSet.setIssuerId("eIDAS");
            dataSet.setTypes("eIDAS");
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            String nowDate = formatter.format(date);
            dataSet.setIssued(nowDate);
            dataSet.setAttributes(receivedAttributes.getAttributes());
            //add them to the data store
            dsArrayList.add(dataSet);
            DataSet[] newDataSet = new DataSet[dsArrayList.size()];
            for (int i = 0; i < newDataSet.length; i++) {
                newDataSet[i] = dsArrayList.get(i);
            }
            ds.setClearData(newDataSet);
            //store them in the session manager
            String updatedDataStoreString = mapper.writeValueAsString(ds);
            requestParams.clear();
            requestParams.add(new NameValuePair("dataStore", updatedDataStoreString));
            requestParams.add(new NameValuePair("variableName", "dsResponse"));
            requestParams.add(new NameValuePair("sessionId", sessionId));
            UpdateDataRequest updateReq = new UpdateDataRequest(sessionId, "dsResponse", updatedDataStoreString);
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
            Log.info("updateSessionData " + resp.getCode().toString());
            if (!resp.getCode().toString().equals("OK")) {
                Log.error("ERROR: " + resp.getError());
                //TODO handle error response
                return new ModelAndView("error");
            }
            Log.info("session " + sessionId + " updated succesfully with user attributes " + user.toString());

            // then retrieve the callback url,
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", sessionId));
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", requestParams, 1), SessionMngrResponse.class);
            Log.info("tried to retrieve session for " + sessionId);
            Log.info("getSession " + resp.getCode().toString());
            if (!resp.getCode().toString().equals("OK")) {
                Log.error("ERROR: " + resp.getError());
                return new ModelAndView("error");
            }
            String callbackUrl = (String) resp.getSessionData().getSessionVariables().get("ClientCallbackAddr");
            Log.info("the callbackUrl for sessionId " + sessionId + "is " + callbackUrl + "!!!!!!!!!!!!");

            //IdP Connector generates a new security token to send to the ACM, by calling get “/sm/generateToken”
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", sessionId));
            requestParams.add(new NameValuePair("sender", paramServ.getParam("RESPONSE_SENDER_ID"))); //[TODO] add correct sender "IdPms001"
            requestParams.add(new NameValuePair("receiver", paramServ.getParam("RESPONSE_RECEOVER_ID"))); //"ACMms001"
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams, 1), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("NEW")) {
                Log.error("ERROR: " + resp.getError());
                return new ModelAndView("error");
            }

            //and open a view that redirects to that callback url by posting a form
            // containing the new security token
            model.addAttribute("callback", callbackUrl);
            model.addAttribute("token", resp.getAdditionalData());
            return new ModelAndView("clientRedirect");
        } catch (IOException ex) {
            Log.error(ex.getLocalizedMessage());
        } catch (NoSuchAlgorithmException ex) {
            Log.error(ex.getLocalizedMessage());
        }

        return null;
    }

}
