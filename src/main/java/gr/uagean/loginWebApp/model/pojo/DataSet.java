/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

/**
 *
 * @author nikos
 */
public class DataSet {

    private String id; //Unique identifier of the set
    private String types; //To define different kinds of datasets, to establish classifications
    private String[] categories; //To define multiple classes where the data set can be grouped.
    private String issuerId; // Name of the attribute that is the ID of the entity that issued the data set, a kind of pointer to the property ID.
    private String subjectId; // Name of the attribute that is the ID of the data owner, a kind of pointer to the attribute ID.
    private String loa; //Level of assurance of the authenticity of the data/authentication
    private String issued; // Date when the data set was retrieved from its source
    private String expiration; // Maximum validity date of the set (empty means permanent)
    private AttributeType[] attributes; // The list of the identity attributes or claims contained on the set
    private String additionalProperties; //
    private Object properties; // description: Dictionary of additional fields of data related to the attributes in the set(strings only) for any specific purpose.
    //type: object

    public DataSet() {
    }

    public DataSet(String id, String types, String[] categories, String issuerId, String subjectId, String loa, String issued, String expiration, AttributeType[] attributes, String additionalProperties, Object properties) {
        this.id = id;
        this.types = types;
        this.categories = categories;
        this.issuerId = issuerId;
        this.subjectId = subjectId;
        this.loa = loa;
        this.issued = issued;
        this.expiration = expiration;
        this.attributes = attributes;
        this.additionalProperties = additionalProperties;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getLoa() {
        return loa;
    }

    public void setLoa(String loa) {
        this.loa = loa;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public AttributeType[] getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeType[] attributes) {
        this.attributes = attributes;
    }

    public String getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataSet{id=").append(id);
        sb.append(", types=").append(types);
        sb.append(", categories=").append(categories);
        sb.append(", issuerId=").append(issuerId);
        sb.append(", subjectId=").append(subjectId);
        sb.append(", loa=").append(loa);
        sb.append(", issued=").append(issued);
        sb.append(", expiration=").append(expiration);
        sb.append(", attributes=").append(attributes);
        sb.append(", additionalProperties=").append(additionalProperties);
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }

}
