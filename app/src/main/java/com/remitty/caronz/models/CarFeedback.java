package com.remitty.caronz.models;

import org.json.JSONObject;

public class CarFeedback {
    private JSONObject data;
    private String comment;
    private Double rate;

    public CarFeedback(JSONObject data) {
        this.data = data;
    }

    public Float getRate() {
        return Float.parseFloat(data.optString("rate"));
    }

    public String getComment() {
        return data.optString("comment");
    }
}
