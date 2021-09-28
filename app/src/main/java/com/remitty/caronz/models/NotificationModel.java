package com.remitty.caronz.models;

import org.json.JSONObject;

public class NotificationModel {
    private JSONObject data;

    public NotificationModel(JSONObject data) {
        this.data = data;
    }

    public String getId() {return data.optString("id");}

    public String getDate() {
        return this.data.optString("updated_at").substring(0, 10);
    }

    public String getMsg() {
        return data.optString("msg");
    }
}
