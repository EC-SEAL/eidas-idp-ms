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
public class AttributeType {

    private String name;
    private String friendlyName;
    private String encoding;
    private String language;
    private boolean mandatory;
    private String[] values;

    public AttributeType() {
    }

    public AttributeType(String name, String friendlyName, String encoding, String language, boolean isMandatory, String[] values) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.encoding = encoding;
        this.language = language;
        this.mandatory = isMandatory;
        this.values = values;
    }

    public boolean setMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

}
