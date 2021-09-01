package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

public class UserModel {
    private JSONObject data;

    public UserModel(JSONObject user) {
        data = user;
    }

    public void setData(JSONObject data){this.data = data;}

    public Integer getId(){
        try {
            return data.getInt("id");
        } catch (JSONException e) {
            return 0;
        }
    }

    public String getFirstName(){
        try {
            return data.getString("first_name");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getLastName(){
        try {
            return data.getString("last_name");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getUserName() {
        return data.optString("first_name") + " " + data.optString("last_name");
    }

    public String getEmail(){
        try {
            return data.getString("email");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getPicture(){
        try {
            String image = data.getString("picture");
            if(!image.startsWith("http"))
                image = UrlController.ASSET_ADDRESS + image;
            return image;
        } catch (JSONException e) {
            return "";
        }
    }

    public String getMobile(){
        try {
            return data.getString("mobile");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getCountryCode(){
        try {
            return data.getString("country_code");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getPhone(){
        return getCountryCode() + getMobile();
    }

    public String getBalance() {return data.optString("wallet_balance");}

    public String getPostalCode(){
        try {
            ProfileModel profile = new ProfileModel(data.getJSONObject("profile"));
            return profile.getPostalCode();
        } catch (JSONException e) {
            return "";
        }
    }

    public String getFirstAddress(){
        try {
            ProfileModel profile = new ProfileModel(data.getJSONObject("profile"));
            return profile.getFirstAddress();
        } catch (JSONException e) {
            return "";
        }
    }

    public String getSecondAddress(){
        try {
            ProfileModel profile = new ProfileModel(data.getJSONObject("profile"));
            return profile.getSecondAddress();
        } catch (JSONException e) {
            return "";
        }
    }

    public String getCountry(){
        try {
            ProfileModel profile = new ProfileModel(data.getJSONObject("profile"));
            return profile.getCountry();
        } catch (JSONException e) {
            return "";
        }
    }

    public String getState(){
        try {
            ProfileModel profile = new ProfileModel(data.getJSONObject("profile"));
            return profile.getState();
        } catch (JSONException e) {
            return "";
        }
    }

    public String getCity(){
        try {
            ProfileModel profile = new ProfileModel(data.getJSONObject("profile"));
            return profile.getCity();
        } catch (JSONException e) {
            return "";
        }
    }


}
