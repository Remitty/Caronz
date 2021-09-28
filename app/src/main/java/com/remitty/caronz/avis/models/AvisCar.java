package com.remitty.caronz.avis.models;

import androidx.annotation.NonNull;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;

public class AvisCar implements Serializable {
    private transient JSONObject data;
    private transient JSONObject rateTotal;
    private transient JSONObject rate;
    private transient JSONObject pay;
    private transient JSONObject category;
    private transient JSONObject features;
    private transient JSONObject capacity;
    public String jsonData;

    public void setData(JSONObject data) {
        this.data = data;
        rateTotal = data.optJSONObject("rate_totals");
        rate = rateTotal.optJSONObject("rate");
        pay = rateTotal.optJSONObject("pay_later");
        category = data.optJSONObject("category");
        features = data.optJSONObject("features");
        capacity = data.optJSONObject("capacity");
        jsonData = data.toString();
    }


    public String getCatName() {
        return category.optString("name");
    }

    public String getMake() { return category.optString("make");}
    public String getModel() { return category.optString("model");}
    public String getTransmission() { return category.optString("vehicle_transmission");}
    public String getVehicleClassName() { return category.optString("vehicle_class_name");}
    public String getVehicleClassCode() { return category.optString("vehicle_class_code");}

    public String getCatImage() {

        String img = category.optString("image_url");
        if(!img.startsWith("http"))
            img = UrlController.ASSET_ADDRESS + img;
        return img;
    }

    public String getPrice() {
        return new DecimalFormat("#,###.##").format(pay.optDouble("reservation_total"));
    }

    public String getCurrency() {return rate.optString("currency");}
    public String getRateCode() {return rate.optString("rate_code");}


    public String getSeats() {
        return capacity.optString("seats");
    }

    public String getDoors() {
        return capacity.optString("doors");
    }

    public boolean hasBluetooth() {return features.optBoolean("bluetooth_equipped");}
    public boolean hasAirCondition() {return features.optBoolean("air_conditioned");}
    public boolean isSmokeFree() {return features.optBoolean("smoke_free");}
    public boolean isConnectedCar() {return features.optBoolean("connected_car");}

}
