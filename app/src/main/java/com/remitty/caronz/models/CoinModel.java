package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinModel {
    private JSONObject data;

    public CoinModel(JSONObject data) {
        this.data = data;
    }

    public String getId() { return data.optString("id");}
    public String getName() { return data.optString("coin_name");}
    public String getSymbol() { return data.optString("coin_symbol");}
    public Double getRate() { return data.optDouble("coin_rate");}
    public String getIcon() {
        String img = "";
        try {
            img = data.getString("icon");
            if(!img.startsWith("http"))
                img = UrlController.IP_ADDRESS + img;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return img;
    }
}
