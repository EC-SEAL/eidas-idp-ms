/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author nikos
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewUpdateDataRequest {

    private String sessionId;
    private String type;
    private String data;
    private String id;

}
