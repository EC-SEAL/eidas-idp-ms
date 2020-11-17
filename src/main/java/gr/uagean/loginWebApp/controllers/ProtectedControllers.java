/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.factory.AttributeSetFactory;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.DataSet;
import gr.uagean.loginWebApp.model.pojo.DataStore;
import gr.uagean.loginWebApp.model.pojo.EidasUser;
import gr.uagean.loginWebApp.model.pojo.EntityMetadata;
import gr.uagean.loginWebApp.model.pojo.NewUpdateDataRequest;
import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import gr.uagean.loginWebApp.model.pojo.UpdateDataRequest;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @RequestMapping(value = {"/", "/eidas-idp", "/is/protected", "/as/protected", "/protected", "/eidas-idp/as/protected", "/eidas-idp/protected",
        "/eidas-idp/is/protected"}, method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView authenticate(@CookieValue(value = "seal") String sessionId,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs, Model model, @AuthenticationPrincipal OAuth2User principal
    ) throws KeyStoreException {

        try {
            Log.info("Reached protected endpoint with sessionId" + sessionId);

            String referer = request.getHeader("Referer"); //Get previous URL before call '/login'
            String eId = principal.getName();
            Map<String, Object> idToken = principal.getAttributes();

            EidasUser user = new EidasUser();
            user.setCurrentFamilyName((String) idToken.get("FamilyName"));
            user.setCurrentGivenName((String) idToken.get("GivenName"));
            user.setDateOfBirth((String) idToken.get("DateOfBirth"));
            user.setProfileName((String) idToken.get("preferred_username"));
            user.setEid(eId);
            user.setPersonIdentifier((String) idToken.get("PersonIdentifier"));

            String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
            List<NameValuePair> requestParams = new ArrayList();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //IdP connector gets the datastore from the session manager and sets the eIDAS dataset object
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", sessionId));
            SessionMngrResponse resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", requestParams, 1), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("OK")) {
                Log.error("ERROR: " + resp.getError());
                return new ModelAndView("error");
            }

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
            Log.info("the callbackUrl for sessionId " + sessionId + " is " + callbackUrl + "!!!!!!!!!!!!");


            /*
                        1. UC101, eidas-idpms reads from SM IdPRequest,IdPMetadata and sets dataStore in SM -->  Dashboard
                        2. UC801, reads from SM IdRequest, IdPMetadata and sets dsResponse and dsMetadata --> RM
                        3. UC802, reads from SM apRequest, apMetadata and sets dsResponse and dsMetadata --> RM
                        4. UC302, reads from SM apRequest, apMetadata and sets and sets dataStore in SM  --> Dashboard

                        match the “rm/response” string stored in the in the ClientCallbackAddr to determine if the response is for the RM

             */
            String id = UUID.randomUUID().toString();
            AttributeSet receivedAttributes = AttributeSetFactory.makeFromEidasResponse(sessionId, id, TypeEnum.AUTHRESPONSE, "issuer", "recipient", user);
            String objectId = "urn:mace:project-seal.eu:id:dataset:eIDAS-IdP:" + "eIDAS_" + user.getPersonIdentifier().split("/")[0] + ":" + URLEncoder.encode(user.getPersonIdentifier(), StandardCharsets.UTF_8.toString());

            if (callbackUrl.contains("rm/response")) {
                EntityMetadata metadata = this.metadataServ.getMetadata();
                //UC801
                if (!StringUtils.isEmpty((String) resp.getSessionData().getSessionVariables().get("IdPMetadata"))) {
                    //attributeSet
                    //store them in the session manager
//                    String dataStoreString = (String) resp.getSessionData().getSessionVariables().get("dataStore");
//                    DataStore ds = makeNewDataStore(dataStoreString,
//                            id, user.getLoa(), receivedAttributes.getAttributes());
                    Log.info("@@@@@updating dsResponse with " + receivedAttributes.toString());
                    updatSessionVariables(sessionMngrUrl, sessionId, objectId, "dsResponse", receivedAttributes);
                    updatSessionVariables(sessionMngrUrl, sessionId, objectId, "dsMetadata", metadata);
                } else {
                    //UC802
                    //store them in the session manager
//                    String dataStoreString = (String) resp.getSessionData().getSessionVariables().get("dataStore");
//                    DataStore ds = makeNewDataStore(dataStoreString,
//                            id, user.getLoa(), receivedAttributes.getAttributes());
                    //TODO dsResponse needs to be an attributeSet
                    Log.info("@@@@@updating dsResponse with " + receivedAttributes.toString());
                    updatSessionVariables(sessionMngrUrl, sessionId, objectId, "dsResponse", receivedAttributes);
                    updatSessionVariables(sessionMngrUrl, sessionId, objectId, "dsMetadata", metadata);
                }
            } else {

                if (!StringUtils.isEmpty((String) resp.getSessionData().getSessionVariables().get("IdPMetadata"))) {
                    // Case UC302
                    String dataStoreString = (String) resp.getSessionData().getSessionVariables().get("dataStore");
                    DataStore ds = makeNewDataStore(dataStoreString,
                            id, user.getLoa(), receivedAttributes.getAttributes());
                    updatSessionVariables(sessionMngrUrl, sessionId, objectId, "dataStore", ds);

                } else {
//                    // Case UC101
                    String dataStoreString = (String) resp.getSessionData().getSessionVariables().get("dataStore");
                    //store them in the session manager
                    DataStore ds = makeNewDataStore(dataStoreString,
                            id, user.getLoa(), receivedAttributes.getAttributes());
                    updatSessionVariables(sessionMngrUrl, sessionId, objectId, "dataStore", ds);

                }

            }

            Log.info("session " + sessionId + " updated succesfully with user attributes " + user.toString());

            //IdP Connector generates a new security token to send to the Client, by calling get “/sm/generateToken”
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", sessionId));
            requestParams.add(new NameValuePair("sender", paramServ.getParam("RESPONSE_SENDER_ID"))); //[TODO] add correct sender "IdPms001"
            if (callbackUrl.contains("rm/response")) {
                requestParams.add(new NameValuePair("receiver", "RMms001")); //"ACMms001"
            } else {
                requestParams.add(new NameValuePair("receiver", paramServ.getParam("RESPONSE_RECEIVER_ID"))); //"ACMms001"
            }
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

    public String updatSessionVariables(String sessionMngrUrl, String sessionId,
            String objectId, String variableName,
            Object updateObject) throws IOException, NoSuchAlgorithmException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String stringifiedObject = mapper.writeValueAsString(updateObject);

        UpdateDataRequest updateReq = new UpdateDataRequest(sessionId, variableName, stringifiedObject);
        SessionMngrResponse resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
        Log.info("updateSessionData " + resp.getCode().toString());
        if (!resp.getCode().toString().equals("OK")) {
            Log.error("ERROR: " + resp.getError());
            return "error";
        }
        Log.info("session " + sessionId + " updated LEGACY API Session succesfully  with user attributes " + stringifiedObject);

        if (variableName.equals("dsResponse") || variableName.equals("dataStore")) {
            NewUpdateDataRequest newReq = new NewUpdateDataRequest();
            newReq.setId(objectId);
            newReq.setSessionId(sessionId);
            newReq.setType("dataSet");

            Log.info(stringifiedObject);

            DataSet ds = mapper.readValue(stringifiedObject, DataSet.class);
            String dataSet = mapper.writeValueAsString(ds);
            newReq.setData(dataSet);

            if (ds.getAttributes() == null) {
                //object was not parsed ok, should reparse it
                DataStore dstore = mapper.readValue(stringifiedObject, DataStore.class);
                String newDataSet = mapper.writeValueAsString(dstore.getClearData()[0]);
                newReq.setData(newDataSet);
            }

            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/new/add",
                    newReq, "application/json", 1), SessionMngrResponse.class);
            Log.info("updateSessionData " + resp.getCode().toString());
            if (!resp.getCode().toString().equals("OK")) {
                Log.error("ERROR: " + resp.getError());
                return "error";
            }
            Log.info("session " + sessionId + " updated NEW API Session succesfully  with objectID" + objectId + "  with user attributes " + stringifiedObject);

            updateReq = new UpdateDataRequest(sessionId, "authenticatedSubject", newReq.getData());
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("OK")) {
                Log.error("ERROR: " + resp.getError());
                return "error";
            }
            Log.info("session " + sessionId + " updated LEGACY API variable  authenticatedSubject with" + stringifiedObject);

        }

        return "ok";
    }

    public DataStore makeNewDataStore(String dataStoreString,
            String id, String loa, AttributeType[] attributes) throws JsonProcessingException {
//        String dataStoreString = (String) resp.getSessionData().getSessionVariables().get("dataStore");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
        DataSet dataSet = new DataSet();
        dataSet.setId(id);
        dataSet.setLoa(loa);
        dataSet.setIssued(id);
        dataSet.setIssuerId("eidasDatasetIssuer");
        dataSet.setType("eIDAS");
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        String nowDate = formatter.format(date);
        dataSet.setIssued(nowDate);

        dataSet.setAttributes(attributes);
        dataSet.setSubjectId("PersonIdentifier");

        //add them to the data store
        dsArrayList.add(dataSet);
        DataSet[] newDataSet = new DataSet[dsArrayList.size()];
        for (int i = 0; i < newDataSet.length; i++) {
            newDataSet[i] = dsArrayList.get(i);

        }
        ds.setClearData(newDataSet);

        return ds;

    }

}
