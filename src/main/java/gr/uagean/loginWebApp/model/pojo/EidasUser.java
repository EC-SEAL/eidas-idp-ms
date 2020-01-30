/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import javax.validation.constraints.NotNull;

/**
 *
 * @author nikos
 */
public class EidasUser {

    //{"firstName":"ΑΝΔΡΕΑΣ, ANDREAS","eid":"GR/GR/ERMIS-11076669","familyName":"ΠΕΤΡΟΥ, PETROU","personIdentifier":"GR/GR/ERMIS-11076669","dateOfBirth":"1980-01-01"}
    private String profileName;
    @NotNull
    private String eid;
    @NotNull
    @JsonAlias({"currentGivenName", "firstName"})
    private String currentGivenName;
    @NotNull
    @JsonAlias({"currentFamilyName", "familyName"})
    private String currentFamilyName;
    private String personIdentifier;
    private String dateOfBirth;
    private String gender;

    @JsonAlias({"loa", "levelOfAssurance"})
    private String loa;

    public EidasUser() {
    }

    public EidasUser(String profileName, String eid, String currentGivenName, String currentFamilyName, String personIdentifier, String dateOfBirth, String gender, String loa) {
        this.profileName = profileName;
        this.eid = eid;
        this.currentGivenName = currentGivenName;
        this.currentFamilyName = currentFamilyName;
        this.personIdentifier = personIdentifier;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.loa = loa;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getCurrentGivenName() {
        return currentGivenName;
    }

    public void setCurrentGivenName(String currentGivenName) {
        this.currentGivenName = currentGivenName;
    }

    public String getCurrentFamilyName() {
        return currentFamilyName;
    }

    public void setCurrentFamilyName(String currentFamilyName) {
        this.currentFamilyName = currentFamilyName;
    }

    public String getPersonIdentifier() {
        return personIdentifier;
    }

    public void setPersonIdentifier(String personIdentifier) {
        this.personIdentifier = personIdentifier;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLoa() {
        return loa;
    }

    public void setLoa(String loa) {
        this.loa = loa;
    }

    @Override
    public String toString() {
        return "EidasUser{" + "loa=" + loa + "profileName=" + profileName + ", eid=" + eid + ", currentGivenName=" + currentGivenName + ", currentFamilyName=" + currentFamilyName + ", personIdentifier=" + personIdentifier + ", dateOfBirth=" + dateOfBirth + ", gender=" + gender + '}';
    }

}
