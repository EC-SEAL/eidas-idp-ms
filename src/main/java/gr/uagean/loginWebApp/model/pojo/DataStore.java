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
//Represents a clear, encrypted and/or signed data set.
public class DataStore {

    private String id; //Unique identifier of the set
    private String encryptedData; //  If the data store is encrypted, this will be set. B64 string
    private String signature; // If the data store is signed, signature goes here. B64 string. Sign always the decrypted dataset.
    private String signatureAlgorithm; //Descriptor of the signature algorithm used.
    private String encryptionAlgorithm; // Descriptor of the encryption algorithm used.
    private DataSet[] clearData; // If the data store is in cleartext, this will be set

    public DataStore() {
    }

    public DataStore(String id, String encryptedData, String signature, String signatureAlgorithm, String encryptionAlgorithm, DataSet[] clearData) {
        this.id = id;
        this.encryptedData = encryptedData;
        this.signature = signature;
        this.signatureAlgorithm = signatureAlgorithm;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.clearData = clearData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public DataSet[] getClearData() {
        return clearData;
    }

    public void setClearData(DataSet[] clearData) {
        this.clearData = clearData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataStore{id=").append(id);
        sb.append(", encryptedData=").append(encryptedData);
        sb.append(", signature=").append(signature);
        sb.append(", signatureAlgorithm=").append(signatureAlgorithm);
        sb.append(", encryptionAlgorithm=").append(encryptionAlgorithm);
        sb.append(", clearData=").append(clearData.toString());
        sb.append('}');
        return sb.toString();
    }

}
