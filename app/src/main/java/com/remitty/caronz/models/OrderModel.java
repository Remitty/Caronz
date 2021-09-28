package com.remitty.caronz.models;

import org.json.JSONObject;

public class OrderModel {
    private JSONObject data;

    public OrderModel(JSONObject data) {
        this.data = data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getId() {
        return data.optString("id");
    }

    public String getTotal(){return data.optString("total");}

    public String getStatus(){return data.optString("status");}

    public String getCarId(){return data.optString("car_id");}

    public String getProcessDate() {return this.data.optString("updated_at").substring(0, 10);}

    public CarModel getCar() {
        return new CarModel(data.optJSONObject("car"));
    }

    public UserModel getOwner() {
        return getCar().getOwner();
    }
}
