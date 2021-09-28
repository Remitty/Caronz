package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RentalModel {

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

    public RentalModel(JSONObject data) {
        this.data = data;
    }

    public RentalModel(){}

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONArray getTimer_array() {
        return timer_array;
    }

    public void setTimer_array(JSONArray timer_array) {
        this.timer_array = timer_array;
    }

    public boolean isIs_show_countDown() {
        return is_show_countDown;
    }

    public void setIs_show_countDown(boolean is_show_countDown) {
        this.is_show_countDown = is_show_countDown;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setIsturned(int isturned) {
        this.isturned = isturned;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(String imageResourceId) {
        if (!imageResourceId.startsWith("http"))
            imageResourceId = UrlController.ASSET_ADDRESS + imageResourceId;
        this.imageResourceId = imageResourceId;
    }
    public String getId() {
        return data.optString("id");
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddTypeFeature() {
        return addTypeFeature;
    }

    public String getBookFrom(){return data.optString("book_from");}

    public String getBookTo(){return data.optString("book_to");}

    public String getBookTotal(){return data.optString("total");}
    public String getSubTotal(){return data.optString("subtotal");}


    public String getBookStatus(){return data.optString("status");}
    public String getPayment(){return data.optString("source");}

    public String getCarId(){return data.optString("car_id");}

    public String getProcessdate() {return this.data.optString("updated_at").substring(0, 10);}

    public Float getRate() {
        return Float.parseFloat(data.optString("rate"));
    }

    public CarModel getCar() {
        return new CarModel(data.optJSONObject("car"));
    }

}