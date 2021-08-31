package com.remitty.caronz.models;

import org.json.JSONException;
import org.json.JSONObject;

public class CreditCard {


    /**
     * Created by admin on 8/4/2017.
     */

        public String id;
        public String user_id;
        public String last_four;
        public String card_id;
        public String brand;
        public String is_default;
        private JSONObject data;

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getCardId() {
        try {
            return data.getString("card_id");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getLastFour() {
        try {
            return data.getString("last_four");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getCardNo() {
        try {
            return "XXXX-XXXX-XXXX-" + data.getString("last_four");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getBrand() {
        try {
            return data.getString("brand");
        } catch (JSONException e) {
            return "Visa";
        }
    }

    public String getCVC() {
        try {
            return data.getString("cvc");
        } catch (JSONException e) {
            return "no data";
        }
    }

    public Boolean isDefault() {
        try {
            return data.getInt("is_default") == 1 ? true: false;
        } catch (JSONException e) {
            return false;
        }
    }

    public String getExpDate() {
        try {
            return data.getString("exp_date");
        } catch (JSONException e) {
            return "no data";
        }
    }

}
