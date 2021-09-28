package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

public class DocumentModel {
    private JSONObject data;

    public DocumentModel(JSONObject document) {
        data = document;
    }

    public void setData(JSONObject data){this.data = data;}

    public String getLicense(){
        try {
            String image = data.getString("license");
            if(!image.startsWith("http"))
                image = UrlController.ASSET_ADDRESS + image;
            return image;
        } catch (JSONException e) {
            return "";
        }
    }

    public String getRegistration(){
        try {
            String image = data.getString("registration");
            if(!image.startsWith("http"))
                image = UrlController.ASSET_ADDRESS + image;
            return image;
        } catch (JSONException e) {
            return "";
        }
    }

    public String getInsurance(){
        try {
            String image = data.getString("insurance");
            if(!image.startsWith("http"))
                image = UrlController.ASSET_ADDRESS + image;
            return image;
        } catch (JSONException e) {
            return "";
        }
    }

    public String getOther1(){
        try {
            String image = data.getString("other1");
            if(!image.startsWith("http"))
                image = UrlController.ASSET_ADDRESS + image;
            return image;
        } catch (JSONException e) {
            return "";
        }
    }

    public String getOther2(){
        try {
            String image = data.getString("other2");
            if(!image.startsWith("http"))
                image = UrlController.ASSET_ADDRESS + image;
            return image;
        } catch (JSONException e) {
            return "";
        }
    }

}
