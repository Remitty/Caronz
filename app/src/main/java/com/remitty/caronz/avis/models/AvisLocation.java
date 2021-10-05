package com.remitty.caronz.avis.models;

import org.json.JSONObject;

import java.io.Serializable;

public class AvisLocation implements Serializable {
    private transient JSONObject data;
    private transient JSONObject address;
    public String jsonData;

    public void setData(JSONObject data) {
        this.data = data;
        this.address = data.optJSONObject("address");
        jsonData = data.toString();
    }

    public void setLocation(JSONObject data) {
        this.data= data;
    }

    public String getAddress() {
        return getAddress1() + ", " + getCity() + ", " + getState() + ", " + getCountry();
    }

    public String getCode() {
        return data.optString("code");
    }
    public String getName() {
        return data.optString("name");
    }
    public String getHours() {
        return data.optString("hours");
    }

    public String getPhone() {
        return data.optString("telephone");
    }

    public String getCountry() {
        return address.optString("country_code");
    }

    private String getState() {return address.optString("state_name");}
    private String getCity() {return address.optString("city");}
    private String getAddress1() {return address.optString("address_line_1");}


}
