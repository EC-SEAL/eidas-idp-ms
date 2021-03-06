/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.dmo;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author nikos
 */
@Entity
@Table(name = "Genders")
public class Gender {

    public final static String UNSPECIFIED = "UNSPECIFIED";
    public final static String MALE = "MALE";
    public final static String FEMALE = "FEMALE";

    @Id
    @Column(name = "gender_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long genderId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "gender")
    private Set<User> users;

    private String name;

    public Gender() {
    }

    public Gender(String name) {
        this.name = name;
    }

    public long getGenderId() {
        return genderId;
    }

    public void setGenderId(long genderId) {
        this.genderId = genderId;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
