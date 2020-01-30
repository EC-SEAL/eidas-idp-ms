/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.factory;

import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeSetStatus;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.EidasUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nikos
 */
public class AttributeSetFactory {

    public static String NameID = "NameID";
    public final static String ATTRIBUTES_KEY = "attributes";
    public final static String METADATA_KEY = "metadata";
    public final static String NOT_BEFORE_KEY = "notBefore";
    public final static String NOT_AFTER_KEY = "notAfter";
    public final static String NAME_ID_KEY = "NameID";

    public static AttributeSet makeFromEidasResponse(String id, TypeEnum type, String issuer, String recipient, EidasUser user) {

        List<AttributeType> attributes = new ArrayList();
        attributes.add(makeAttType("FamilyName", "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", user.getCurrentFamilyName()));
        attributes.add(makeAttType("GivenName", "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", user.getCurrentGivenName()));
        attributes.add(makeAttType("DateOfBirth", "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", user.getCurrentGivenName()));
        attributes.add(makeAttType("PersonIdentifier", "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", user.getPersonIdentifier()));
        attributes.add(makeAttType("LevelOfAssurance", "http://eidas.europa.eu/LoA", user.getPersonIdentifier()));

        Map<String, String> metadataProperties = new HashMap();
        metadataProperties.put("levelOfAssurance", user.getLoa());
        metadataProperties.put("NameID", user.getPersonIdentifier());

        AttributeSetStatus atrSetStatus = new AttributeSetStatus();
        atrSetStatus.setCode(AttributeSetStatus.CodeEnum.OK);

        AttributeType[] attrArray = new AttributeType[attributes.size()];
        return new AttributeSet(id, type, issuer, recipient, attributes.toArray(attrArray),
                metadataProperties, null, user.getLoa(), null, null, atrSetStatus);
    }

    public static AttributeType makeAttType(String friendlyName, String name, String value) {
        return new AttributeType(name, friendlyName, "UTF-8", "N/A", true, new String[]{value});
    }

}
