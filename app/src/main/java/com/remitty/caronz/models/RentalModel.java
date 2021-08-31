package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
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

    public String getAdViews() {
        return adViews;
    }

    public void setAdViews(String adViews) {
        this.adViews = adViews;
    }

    public int getIsturned() {
        return isturned;
    }

    public void setIsturned(int isturned) {
        this.isturned = isturned;
    }

    public int getIsfav() {
        return isfav;
    }

    public void setIsfav(int isfav) {
        this.isfav = isfav;
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

    public boolean isSearchItem() {
        return isSearchItem;
    }

    public void setSearchItem(boolean searchItem) {
        isSearchItem = searchItem;
    }

    public String getId() {
        return data.optString("id");
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFavBtnText() {
        return favBtnText;
    }

    public void setFavBtnText(String favBtnText) {
        this.favBtnText = favBtnText;
    }

    public String getFavColorCode() {
        return favColorCode;
    }

    public void setFavColorCode(String favColorCode) {
        this.favColorCode = favColorCode;
    }

    public String getAddTypeFeature() {
        return addTypeFeature;
    }

    public void setAddTypeFeature(String addTypeFeature) {
        this.addTypeFeature = addTypeFeature;
    }

    public void setFeatureType(boolean featureType) {
        this.isFeatureType = featureType;
    }

    public boolean getFeaturetype() {
        return this.isFeatureType;
    }

    public void setBookFrom(String from){this.bookFrom = from;}

    public String getBookFrom(){return data.optString("book_from");}

    public void setBookTo(String to){this.bookTo = to;}

    public String getBookTo(){return data.optString("book_to");}

    public void setBookTotal(String total){this.bookTotal = total;}

    public String getBookTotal(){return data.optString("total");}

    public void setBookDuration(String duration){this.bookDuration = duration;}

    public String getBookDuration(){return data.optString("duration");}

    public void setBookCustomer(String customer){this.bookCustomerName = customer;}

    public String getBookCustomer(){return this.bookCustomerName;}

    public void setBookStatus(int status){this.bookStatus = status;}

    public String getBookStatus(){return data.optString("status");}

    public void setBookId(String id){this.bookId = id;}

    public String getCarId(){return data.optString("car_id");}

    public void setBookTranssaction(String transaction){this.bookTransaction = transaction;}

    public String getBookTransaction(){return this.bookTransaction;}

    public String getProcessdate() {return this.data.optString("updated_at").substring(0, 10);}

    public void setFee(String fee) {this.fee = fee;}
    public String getFee(){return this.fee;}

    public CarModel getCar() {
        return new CarModel(data.optJSONObject("car"));
    }

}