package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONObject;

public class HomeCatListModel {
    private String title;
    private String thumbnail;
    private String id;
    private String bgcolor;
    private JSONObject data;

    public HomeCatListModel(JSONObject object) {
        data = object;
    }


    public String getTitle() {
        title = data.optString("name");
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBGColor(){return bgcolor;}

    public void setBGColor(String color){bgcolor = color;}

    public String getThumbnail() {
        thumbnail = data.optString("img");
        if(!thumbnail.startsWith("http"))
            thumbnail = UrlController.ASSET_ADDRESS + thumbnail;
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        if(!thumbnail.startsWith("http"))
            thumbnail = UrlController.ASSET_ADDRESS + thumbnail;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        id = data.optString("id");
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
