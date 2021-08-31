package com.remitty.caronz.models;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileModel {
    private JSONObject data;

    public ProfileModel(JSONObject profile) {
        data = profile;
    }

    public void setData(JSONObject data){this.data = data;}

    public String getPostalCode(){
        try {
            return data.getString("postalcode");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getFirstAddress(){
        try {
            return data.getString("address");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getSecondAddress(){
        try {
            return data.getString("address2");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getCountry(){
        try {
            return data.getString("country");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getState(){
        try {
            return data.getString("state");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getCity(){
        try {
            return data.getString("city");
        } catch (JSONException e) {
            return "";
        }
    }

}
