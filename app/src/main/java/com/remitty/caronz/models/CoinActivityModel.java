package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

public class CoinActivityModel {
    private JSONObject data;

    public CoinActivityModel(JSONObject data) {
        this.data = data;
    }

    public String getId() { return data.optString("id");}
    public String getSymbol() { return data.optString("currency");}
    public Double getAmount() { return data.optDouble("amount");}
    public String getDate() { return data.optString("updated_at").substring(0, 10);}
    public String getAddress() { return data.optString("address");}
    public String getTxn() { return data.optString("txn_id");}

}
