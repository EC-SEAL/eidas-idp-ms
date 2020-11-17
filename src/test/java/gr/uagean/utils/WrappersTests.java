/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.model.pojo.DataSet;
import gr.uagean.loginWebApp.model.pojo.DataStore;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class WrappersTests {

    @Test
    public void wrapEidasResponseToDataset() throws JsonProcessingException {
        String response = "{\"id\":\"32f489b1-87e8-4952-9214-01513b44f431\",\"type\":\"AuthResponse\",\"issuer\":\"issuer\",\"recipient\":\"recipient\",\"attributes\":[{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\",\"friendlyName\":\"FamilyName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΠΕΤΡΟΥ PETROU\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName\",\"friendlyName\":\"GivenName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΑΝΔΡΕΑΣ ANDREAS\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\",\"friendlyName\":\"DateOfBirth\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"1980-01-01\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\",\"friendlyName\":\"PersonIdentifier\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"GR/GR/ERMIS-11076669\"]},{\"name\":\"http://eidas.europa.eu/LoA\",\"friendlyName\":\"LevelOfAssurance\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[null]}],\"properties\":{\"NameID\":\"GR/GR/ERMIS-11076669\",\"levelOfAssurance\":null},\"inResponseTo\":\"cc664c58-7fcd-4cab-8b2e-0d0256764f66\",\"loa\":null,\"notBefore\":null,\"notAfter\":null,\"status\":{\"code\":\"OK\",\"subcode\":null,\"message\":null} }";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DataSet ds = mapper.readValue(response, DataSet.class);
        ds.getAttributes();

    }

    @Test
    public void testParsingLegacyAPI() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String stringObj = "{\"id\":\"a12b93e4-fb3b-4d59-ba0c-faba8e89bba7\",\"encryptedData\":null,\"signature\":null,\"signatureAlgorithm\":null,\"encryptionAlgorithm\":null,\"clearData\":[{\"id\":\"c5be47de-e233-45fd-b42b-6b914c9ad063\",\"type\":\"eIDAS\",\"categories\":null,\"issuerId\":\"eIDAS\",\"subjectId\":null,\"loa\":null,\"issued\":\"Wed, 7 Oct 2020 12:36:33 GMT\",\"expiration\":null,\"attributes\":[{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\",\"friendlyName\":\"FamilyName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΠΑΛΙΟΚΩΣΤΑ PALIOKOSTA\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName\",\"friendlyName\":\"GivenName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΧΡΙΣΤΙΝΑ CHRISTINA\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\",\"friendlyName\":\"DateOfBirth\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"1980-01-01\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\",\"friendlyName\":\"PersonIdentifier\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"GR/GR/ERMIS-58333947\"]},{\"name\":\"http://eidas.europa.eu/LoA\",\"friendlyName\":\"LevelOfAssurance\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[null]}],\"properties\":null},{\"id\":\"7c537973-2847-46a7-b7be-fdb7685a01bc\",\"type\":\"eIDAS\",\"categories\":null,\"issuerId\":\"eIDAS\",\"subjectId\":null,\"loa\":null,\"issued\":\"Wed, 7 Oct 2020 12:37:14 GMT\",\"expiration\":null,\"attributes\":[{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\",\"friendlyName\":\"FamilyName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΠΑΛΙΟΚΩΣΤΑ PALIOKOSTA\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName\",\"friendlyName\":\"GivenName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΧΡΙΣΤΙΝΑ CHRISTINA\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\",\"friendlyName\":\"DateOfBirth\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"1980-01-01\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\",\"friendlyName\":\"PersonIdentifier\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"GR/GR/ERMIS-58333947\"]},{\"name\":\"http://eidas.europa.eu/LoA\",\"friendlyName\":\"LevelOfAssurance\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[null]}],\"properties\":null}]}";
        DataStore dst = mapper.readValue(stringObj, DataStore.class);
        DataSet ds = dst.getClearData()[0];

        assertEquals(ds.getAttributes().length > 0, true);
    }

//    @Test
    public void testPArsing() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String stringifiedObject = "{\"id\":\"a0e3012a-23c0-4909-96ae-d887c0314dbd\",\"encryptedData\":null,\"signature\":null,\"signatureAlgorithm\":null,\"encryptionAlgorithm\":null,\"clearData\":[{\"id\":\"e95150ae-4852-444a-ad14-043f81586953\",\"type\":\"eIDAS\",\"categories\":null,\"issuerId\":\"eIDAS\",\"subjectId\":null,\"loa\":null,\"issued\":\"Fri, 30 Oct 2020 14:39:47 GMT\",\"expiration\":null,\"attributes\":[{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName\",\"friendlyName\":\"FamilyName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΠΑΛΙΟΚΩΣΤΑ PALIOKOSTA\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName\",\"friendlyName\":\"GivenName\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"ΧΡΙΣΤΙΝΑ CHRISTINA\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/DateOfBirth\",\"friendlyName\":\"DateOfBirth\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"1980-01-01\"]},{\"name\":\"http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier\",\"friendlyName\":\"PersonIdentifier\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[\"GR/GR/ERMIS-58333947\"]},{\"name\":\"http://eidas.europa.eu/LoA\",\"friendlyName\":\"LevelOfAssurance\",\"encoding\":\"UTF-8\",\"language\":\"N/A\",\"values\":[null]}],\"properties\":null}]}";
        DataStore dstore = mapper.readValue(stringifiedObject, DataStore.class);
        String newDataSet = mapper.writeValueAsString(dstore.getClearData()[0]);
//        assertEquals(newDataSet, true);
    }

}
