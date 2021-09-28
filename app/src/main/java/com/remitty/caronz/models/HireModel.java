package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONObject;

public class HireModel {

    public boolean isFeatureType = false;
    private String id;
    private String cardName;
    private String path;
    private String price;
    private String date;
    private String location;
    private String adViews;
    private String imageResourceId;
    private int isfav;
    private int isturned;
    private boolean isSearchItem;
    private String favBtnText;
    private String favColorCode;
    private String addTypeFeature;
    private String type;
    private String processdate;
    private JSONArray timer_array;
    private boolean is_show_countDown = false;
    private String fee;

    private String bookFrom, bookTo, bookTotal, bookDuration, bookTransaction, bookId, bookCustomerName;
    private int bookStatus;

    private JSONObject data;

    public HireModel(JSONObject data) {this.data = data;}

    public HireModel(){}

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getId() {
        return data.optString("id");
    }

    public String getPickupLocation() {
        return data.optString("s_address");
    }

    public String getDropoffLocation() {
        return data.optString("d_address");
    }

    public String getBookFrom(){return data.optString("start_time");}

    public String getEstDistance(){return data.optString("est_distance");}
    public String getEstTime(){return data.optString("est_time");}

    public String getBookTotal(){return data.optString("total");}
    public String getSubTotal(){return data.optString("subtotal");}
    public String getPayment(){return data.optString("source");}

    public String getBookStatus(){return data.optString("status");}

    public String getCarId(){return data.optString("car_id");}

    public String getProcessDate() {return this.data.optString("updated_at").substring(0, 10);}

    public Float getRate() {
        return Float.parseFloat(data.optString("rate"));
    }

    public CarModel getCar() {
        return new CarModel(data.optJSONObject("car"));
    }

    public UserModel getOwner() {
        return getCar().getOwner();
    }

}