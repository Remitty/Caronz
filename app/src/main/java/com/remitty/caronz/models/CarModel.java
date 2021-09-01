package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CarModel {
    private JSONObject data;
    private String id;
    private String name;
    private String catName;
    private String catId;
    private String description;
    private String seats;
    private String speed;
    private String rentPrice;
    private String salePrice;
    private String images;
    private String status;
    private Double rate;
    private JSONArray feedbacks;

    public CarModel(JSONObject data) {
        this.data = data;
    }

    public String getId() {
        return data.optString("id");
    }

    public String getName() {
        return data.optString("name");
    }

    public Integer getSellerId() {
        return data.optInt("seller_id");
    }

    public String getCatName() {
        JSONObject cat = null;
        try {
            cat = data.getJSONObject("category");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return cat.optString("name");
    }

    public String getCatImage() {
        JSONObject cat = null;
        try {
            cat = data.getJSONObject("category");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        String img = cat.optString("img");
        if(!img.startsWith("http"))
            img = UrlController.ASSET_ADDRESS + img;
        return img;
    }

    public int getCatId() {
        JSONObject cat = null;
        try {
            cat = data.getJSONObject("category");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        return cat.optInt("id");
    }

    public String getDescription() {
        try {
            String str = data.getString("description");
            if(str.equals("null"))
             str = "";
            return str;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
    public String getStatus() {
        return data.optString("status");
    }

    public String getPrice() {
        return data.optString("price");
    }

    public boolean isRental() {
        String service = data.optString("service");
        if(service.equals("rent")) return true;
        else return false;
    }

    public boolean isBuy() {
        String service = data.optString("service");
        if(service.equals("buy")) return true;
        else return false;
    }

    public boolean isHire() {
        String service = data.optString("service");
        if(service.equals("hire")) return true;
        else return false;
    }

    public String getSeats() {
        return data.optString("seat");
    }

    public String getLocation() { return data.optString("location");}

    public String getDistance() {
        return data.optString("distance") + " Mile";
    }

    public String getTransmission() { return data.optString("transmission");}

    public JSONArray getImages() {
        try {
            JSONArray images = data.getJSONArray("images");
            return images;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFirstImage() {
        try {
            String img = "";
            JSONArray images = data.getJSONArray("images");
            if(images.length() > 0) {
                JSONObject image = images.getJSONObject(0);
                img = image.optString("thumb");
                if (!img.startsWith("http")) {
                    img = UrlController.ASSET_ADDRESS + img;
                }
            } else {
                img = getCatImage();
            }
            return img;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Float getRate() {
        return Float.parseFloat(data.optString("rate"));
    }

    public ArrayList<CarFeedback> getFeedbacks() {
        try {
            ArrayList<CarFeedback> feedbackList = new ArrayList<>();
            JSONArray feedbacks = data.getJSONArray("feedback");
            feedbackList.clear();
            for (int i = 0; i < feedbacks.length(); i ++) {
                CarFeedback feedback = new CarFeedback(feedbacks.getJSONObject(i));
                feedbackList.add(feedback);
            }
            return feedbackList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getLat() {return data.optString("latitude");}
    public String getLng() {return data.optString("longitude");}
    public UserModel getOwner() {
        try {
            UserModel owner = new UserModel(data.getJSONObject("owner"));
            return owner;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
