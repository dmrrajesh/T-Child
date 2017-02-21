package com.rajeshsaini.dmr.demo.models;

import android.util.Log;

import com.rajeshsaini.dmr.demo.credential.Admin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by DMR on 2/1/2017.
 */
public class User implements Serializable{
    private String id;
    private String name;
    private String mobile;
    private String gender;
    private String email;
    private String address;
    private String pic;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public static User fromJSON(JSONObject object){
        User user = new User();
        try{
            if(object.has(Admin.MOBILE)){
                user.setMobile(object.getString(Admin.MOBILE));
            }
            if(object.has(Admin.USER_ID)){
                user.setId(object.getString(Admin.USER_ID));
            }
        }catch (JSONException e){
            e.printStackTrace();
            Log.e("Error User.java",e.getMessage());
        }
        return user;
    }
}
