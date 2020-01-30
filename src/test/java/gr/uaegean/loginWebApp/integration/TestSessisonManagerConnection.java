/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.loginWebApp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.LoginWebAppApplication;
import gr.uagean.loginWebApp.MemCacheConfig;
import gr.uagean.loginWebApp.TestRestControllersConfig;
import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import gr.uagean.loginWebApp.model.pojo.UpdateDataRequest;
import gr.uagean.loginWebApp.service.HttpSignatureService;
import gr.uagean.loginWebApp.service.KeyStoreService;
import gr.uagean.loginWebApp.service.MSConfigurationService;
import gr.uagean.loginWebApp.service.ParameterService;
import gr.uagean.loginWebApp.service.impl.HttpSignatureServiceImpl;
import gr.uagean.loginWebApp.service.impl.MSConfigurationServiceImpl;
import gr.uagean.loginWebApp.service.impl.NetworkServiceImpl;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LoginWebAppApplication.class, TestRestControllersConfig.class, MemCacheConfig.class})
@AutoConfigureMockMvc
public class TestSessisonManagerConnection {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private KeyStoreService keyServ;

    @Autowired
    private ParameterService paramServ;

    private NetworkServiceImpl netServ;
    private ObjectMapper mapper;

    @Before
    public void init() throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        mapper = new ObjectMapper();
        Key signingKey = this.keyServ.getHttpSigningKey();
        String fingerPrint = DigestUtils.sha256Hex(keyServ.getHttpSigPublicKey().getEncoded());
        HttpSignatureService sigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        netServ = new NetworkServiceImpl(this.keyServ);
    }

    @Test
    public void testFullFlow() throws IOException, NoSuchAlgorithmException, Exception {
        String hostUrl = "https://esmo-gateway.eu";

        String uri = "/sm/startSession";
        List<NameValuePair> postParams = new ArrayList();

        SessionMngrResponse resp = this.mapper.readValue(netServ.sendPostForm(hostUrl, uri, postParams, 1), SessionMngrResponse.class);
        System.out.println(resp.getCode());
        String sessionId = resp.getSessionData().getSessionId();

        AttributeType[] attrType = new AttributeType[2];
        String[] values = new String[1];
        AttributeType att1 = new AttributeType("someURI", "CurrentFamilyName", "UTF-8", "en", true, values);
        AttributeType att2 = new AttributeType("someURI", "CurrentGivenName", "UTF-8", "en", true, values);
        attrType[0] = att1;
        attrType[1] = att2;
        AttributeSet attrSet = new AttributeSet("id", TypeEnum.AUTHRESPONSE, "ACMms001", "IDPms001", attrType, new HashMap<>(), null, "low", null, null, null);

        ObjectMapper mapper = new ObjectMapper();
        String attrSetString = mapper.writeValueAsString(attrSet);
        uri = "/sm/updateSessionData";
        UpdateDataRequest updateDR = new UpdateDataRequest();
        updateDR.setSessionId(sessionId);
        updateDR.setVariableName("idpRequest");
        updateDR.setDataObject(attrSetString);
        resp = this.mapper.readValue(netServ.sendPostBody(hostUrl, uri, updateDR, "application/json", 1), SessionMngrResponse.class);

        //"{\"spRequest\":{\"issuer\":\"https:\/\/moodle.uji.es\/saml\/sp\/metadata.xml\",\"type\":\"Request\",\"recipient\":null,\"id\":\"6c0f70a8-f32b-4535-b5f6-0d596c52813a\",\"attributes\":[{\"name\":\"http:\/\/eidas.europa.eu\/attributes\/naturalperson\/CurrentGivenName\",\"friendlyName\":\"CurrentGivenName\",\"isMandatory\":true},{\"name\":\"http:\/\/eidas.europa.eu\/attributes\/naturalperson\/PersonIdentifier\",\"friendlyName\":\"PersonIdentifier\",\"isMandatory\":true},{\"name\":\"http:\/\/eidas.europa.eu\/attributes\/naturalperson\/CurrentFamilyName\",\"friendlyName\":\"FamilyName\",\"isMandatory\":true},{\"name\":\"http:\/\/eidas.europa.eu\/attributes\/naturalperson\/DateOfBirth\",\"friendlyName\":\"DateOfBirth\",\"isMandatory\":true},{\"name\":\"eduPersonAffiliation\",\"isMandatory\":false}],\"properties\":{\"LoA\":\"http:\/\/eidas.europa.eu\/LoA\/substantial\",\"AuthnContext-Comparison\":\"minimum\",\"NameIDPolicy-AllowCreate\":\"true\",\"NameIDPolicy-Format\":\"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent\",\"SPType\":\"public\",\"ProviderName\":\"Q2891006E_EA0018173\",\"IssueInstant\":\"2018-12-20T12:35:48Z\"}},\"spMetadata\":{\"entityId\":\"https:\/\/moodle.uji.es\/saml\/sp\/metadata.xml\",\"defaultDisplayName\":\"UJI Virtual Learning Service\",\"location\":\"ES|Spain\",\"protocol\":\"SAML2-EIDAS\",\"microservice\":[\"SAMLms001\"],\"endpoints\":{\"type\":\"AssertionConsumerService\",\"method\":\"HTTP-POST\",\"url\":\"https:\/\/moodle.uji.es\/saml\/sp\/acs.php\"},\"securityKeys\":[{\"keyType\":\"RSAPublicKey\",\"usage\":\"signing\",\"key\":\"MDAACaFgw...xFgy=\"},{\"keyType\":\"RSAPublicKey\",\"usage\":\"encryption\",\"key\":\"MDAACaFgw...xFgy=\"}],\"encryptResponses\":false,\"supportedEncryptionAlg\":[\"AES256\",\"AES512\"],\"signResponses\":true,\"supportedSigningAlg\":[\"RSA-SHA256\"]}}" }
        String spRequest = "{\"id\":\"_d645d111cf100dfa46ace16ed3b208f0f2e867db83\",\"type\":\"Request\",\"issuer\":\"https:\\/\\/clave.sir2.rediris.es\\/module.php\\/saml\\/sp\\/saml2-acs.php\\/q2891006e_ea0002678\",\"recipient\":null,\"inResponseTo\":null,\"loa\":\"http:\\/\\/eidas.europa.eu\\/LoA\\/low\",\"notBefore\":\"2019-03-05T15:11:41Z\",\"notAfter\":\"2019-03-05T15:16:41Z\",\"status\":{\"code\":null,\"subcode\":null,\"message\":null},\"attributes\":[{\"name\":\"http:\\/\\/eidas.europa.eu\\/attributes\\/naturalperson\\/PersonIdentifier\",\"friendlyName\":\"PersonIdentifier\",\"encoding\":null,\"language\":null,\"isMandatory\":true,\"values\":null},{\"name\":\"http:\\/\\/eidas.europa.eu\\/attributes\\/naturalperson\\/CurrentGivenName\",\"friendlyName\":\"FirstName\",\"encoding\":null,\"language\":null,\"isMandatory\":true,\"values\":null},{\"name\":\"http:\\/\\/eidas.europa.eu\\/attributes\\/naturalperson\\/CurrentFamilyName\",\"friendlyName\":\"FamilyName\",\"encoding\":null,\"language\":null,\"isMandatory\":true,\"values\":null}],\"properties\":{\"SAML_RelayState\":\"\",\"SAML_RemoteSP_RequestId\":\"_193600a923e1959d375e21fb3d216879\",\"SAML_ForceAuthn\":true,\"SAML_isPassive\":false,\"SAML_NameIDFormat\":\"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent\",\"SAML_AllowCreate\":\"true\",\"SAML_ConsumerURL\":\"http:\\/\\/lab9054.inv.uji.es\\/~paco\\/clave\\/secure.php?aaaa=1&bbbb=2\",\"SAML_Binding\":\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\",\"EIDAS_ProviderName\":\"ojetecalor_uojetecalor\",\"EIDAS_IdFormat\":\"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent\",\"EIDAS_SPType\":\"public\",\"EIDAS_Comparison\":\"minimum\",\"EIDAS_LoA\":\"http:\\/\\/eidas.europa.eu\\/LoA\\/low\",\"EIDAS_country\":null}}";
        String spMetaData = "{\"entityId\":\"https:\\/\\/clave.sir2.rediris.es\\/module.php\\/saml\\/sp\\/saml2-acs.php\\/q2891006e_ea0002678\",\"defaultDisplayName\":null,\"displayNames\":null,\"logo\":null,\"location\":null,\"protocol\":null,\"claims\":null,\"microservice\":[\"SAMLms_0001\"],\"encryptResponses\":false,\"supportedEncryptionAlg\":[null],\"signResponses\":null,\"supportedSigningAlg\":[null],\"endpoints\":[{\"type\":\"AssertionConsumerService\",\"method\":\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\",\"url\":\"http:\\/\\/lab9054.inv.uji.es\\/~paco\\/clave\\/secure.php\"}],\"securityKeys\":[{\"keyType\":\"X509Certificate\",\"usage\":\"encryption\",\"key\":\"MIIDODCCAqGgAwIBAgIJAMqU+wr6z\\/l5MA0GCSqGSIb3DQEBCwUAMIG0MRUwEwYKCZImiZPyLGQBGRYFY2xhdmUxFDASBgoJkiaJk\\/IsZAEZFgRzaXIyMRcwFQYKCZImiZPyLGQBGRYHcmVkaXJpczESMBAGCgmSJomT8ixkARkWAmVzMR4wHAYDVQQKDBVjbGF2ZS5zaXIyLnJlZGlyaXMuZXMxGDAWBgNVBAsMD0NlcnRpZmljYWRvIFNQVDEeMBwGA1UEAwwVY2xhdmUuc2lyMi5yZWRpcmlzLmVzMB4XDTE1MDkwMjExNTI0MVoXDTI1MDkwMTExNTI0MVowgbQxFTATBgoJkiaJk\\/IsZAEZFgVjbGF2ZTEUMBIGCgmSJomT8ixkARkWBHNpcjIxFzAVBgoJkiaJk\\/IsZAEZFgdyZWRpcmlzMRIwEAYKCZImiZPyLGQBGRYCZXMxHjAcBgNVBAoMFWNsYXZlLnNpcjIucmVkaXJpcy5lczEYMBYGA1UECwwPQ2VydGlmaWNhZG8gU1BUMR4wHAYDVQQDDBVjbGF2ZS5zaXIyLnJlZGlyaXMuZXMwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOtBus8tyx2JFH4ILKRfvnJ+Eyb0UG1wOZm0hMutS0MvNQuvBZVytR8lVMFqlRX7U1+FP6O10c2GDniuom3v01uq2guHlu8omR3Tj54ySJf4y7m4b42i8iU+uy3ZK7voPHcyB\\/zKEDnDxVc5KmtioLuk\\/3M9Ofz+Xsed3yCCfMb1AgMBAAGjUDBOMB0GA1UdDgQWBBSSe7SJPTtSLi+ObAQD\\/8QhvORUPzAfBgNVHSMEGDAWgBSSe7SJPTtSLi+ObAQD\\/8QhvORUPzAMBgNVHRMEBTADAQH\\/MA0GCSqGSIb3DQEBCwUAA4GBAF7Md4GMmPl92hUBq1LOOM4Jl6J\\/nHSYLkb3SYvQUyiHOcsU2NaXCg6QlrJf9T+kG3XdAv550cNhLtkbiF2stnQByX1OHPY9kIudyQ3\\/c7DHFRfi3kkBzL4T1AGdn9PvzpQGtDL3owLsI3H5smfhA8ApogJkB5C7gzj6U9m1ZAYz\"},{\"keyType\":\"X509Certificate\",\"usage\":\"signing\",\"key\":\"MIIDODCCAqGgAwIBAgIJAMqU+wr6z\\/l5MA0GCSqGSIb3DQEBCwUAMIG0MRUwEwYKCZImiZPyLGQBGRYFY2xhdmUxFDASBgoJkiaJk\\/IsZAEZFgRzaXIyMRcwFQYKCZImiZPyLGQBGRYHcmVkaXJpczESMBAGCgmSJomT8ixkARkWAmVzMR4wHAYDVQQKDBVjbGF2ZS5zaXIyLnJlZGlyaXMuZXMxGDAWBgNVBAsMD0NlcnRpZmljYWRvIFNQVDEeMBwGA1UEAwwVY2xhdmUuc2lyMi5yZWRpcmlzLmVzMB4XDTE1MDkwMjExNTI0MVoXDTI1MDkwMTExNTI0MVowgbQxFTATBgoJkiaJk\\/IsZAEZFgVjbGF2ZTEUMBIGCgmSJomT8ixkARkWBHNpcjIxFzAVBgoJkiaJk\\/IsZAEZFgdyZWRpcmlzMRIwEAYKCZImiZPyLGQBGRYCZXMxHjAcBgNVBAoMFWNsYXZlLnNpcjIucmVkaXJpcy5lczEYMBYGA1UECwwPQ2VydGlmaWNhZG8gU1BUMR4wHAYDVQQDDBVjbGF2ZS5zaXIyLnJlZGlyaXMuZXMwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOtBus8tyx2JFH4ILKRfvnJ+Eyb0UG1wOZm0hMutS0MvNQuvBZVytR8lVMFqlRX7U1+FP6O10c2GDniuom3v01uq2guHlu8omR3Tj54ySJf4y7m4b42i8iU+uy3ZK7voPHcyB\\/zKEDnDxVc5KmtioLuk\\/3M9Ofz+Xsed3yCCfMb1AgMBAAGjUDBOMB0GA1UdDgQWBBSSe7SJPTtSLi+ObAQD\\/8QhvORUPzAfBgNVHSMEGDAWgBSSe7SJPTtSLi+ObAQD\\/8QhvORUPzAMBgNVHRMEBTADAQH\\/MA0GCSqGSIb3DQEBCwUAA4GBAF7Md4GMmPl92hUBq1LOOM4Jl6J\\/nHSYLkb3SYvQUyiHOcsU2NaXCg6QlrJf9T+kG3XdAv550cNhLtkbiF2stnQByX1OHPY9kIudyQ3\\/c7DHFRfi3kkBzL4T1AGdn9PvzpQGtDL3owLsI3H5smfhA8ApogJkB5C7gzj6U9m1ZAYz\"}],\"otherData\":{\"dialect\":\"eidas\",\"subdialect\":\"eidas\",\"spApplication\":\"q12345678a_ea0004242\",\"issuer\":null,\"sign.logout\":null,\"redirect.sign\":null,\"redirect.validate\":null}}";

        updateDR = new UpdateDataRequest();
        updateDR.setSessionId(sessionId);
        updateDR.setVariableName("spRequest");
        updateDR.setDataObject(spRequest);
        resp = this.mapper.readValue(netServ.sendPostBody(hostUrl, uri, updateDR, "application/json", 1), SessionMngrResponse.class);
        System.out.println(resp.getCode().toString() + "1!!!!!!!!!");

        updateDR = new UpdateDataRequest();
        updateDR.setSessionId(sessionId);
        updateDR.setVariableName("spMetaData");
        updateDR.setDataObject(spMetaData);
        resp = this.mapper.readValue(netServ.sendPostBody(hostUrl, uri, updateDR, "application/json", 1), SessionMngrResponse.class);
        System.out.println(resp.getCode().toString());

        updateDR = new UpdateDataRequest();
        updateDR.setSessionId(sessionId);
        updateDR.setVariableName("ClientCallbackAddr");
        updateDR.setDataObject("https://www.test.com/callback");
        resp = this.mapper.readValue(netServ.sendPostBody(hostUrl, uri, updateDR, "application/json", 1), SessionMngrResponse.class);
        System.out.println(resp.getCode().toString() + "1!!!!!!!!!");

        uri = "/sm/generateToken";
        postParams.clear();
        postParams.add(new NameValuePair("sessionId", sessionId));
        postParams.add(new NameValuePair("sender", "ACMms001"));
        postParams.add(new NameValuePair("receiver", "APms003"));
        resp = this.mapper.readValue(netServ.sendGet(hostUrl, uri, postParams, 1), SessionMngrResponse.class);
        String token = resp.getAdditionalData();

        System.out.println(token);
    }

    @Test
    public void getEndpointFromMSConfig() throws InvalidKeySpecException, IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        MSConfigurationService msConf = new MSConfigurationServiceImpl(paramServ, netServ, keyServ);
        assertEquals(msConf.getMsEndpointByIdAndApiCall("ACMms001", "acmRequest"), "https://dss1.aegean.gr:8073/acm/request");

    }

}
