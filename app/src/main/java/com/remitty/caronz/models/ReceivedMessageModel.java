package com.remitty.caronz.models;

import com.remitty.caronz.utills.UrlController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReceivedMessageModel {

    private JSONObject data;
    private CarModel post;

    public void setData(JSONObject data) {
        this.data = data;
        setPost();
    };

    public String getId() {
        return data.optString("id");
    }

    public boolean isMessageRead() {
        try {
            return data.getBoolean("read");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getLastSenderName() {
        return data.optString("last_sender_name");
    }

    public String getOtherName() {
        return data.optString("other_name");
    }

    public String getLastMessage() {
        return data.optString("message");
    }

    public String getTopic() {
        return post.getName();
    }

    public String getTumbnail() {
        return post.getFirstImage();
    }

    public String getOtherProfile() {
        String profile = data.optString("other_profile");
        if(!profile.startsWith("http"))
            profile = UrlController.IP_ADDRESS+"storage/"+profile;
        return profile;
    }

    public String getReceiver_id() {
        return data.optString("receiver_id");
    }

    public boolean isMine() {
        try {
            return data.getBoolean("isMine");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getChatTime() {
        return data.optString("time");
    }

//    public ArrayList<Message> getMessages() {
//        try {
//            ArrayList<Message> msgList = new ArrayList<>();
//            JSONArray messages = new JSONArray(data.getJSONArray("messages"));
//
//            for (int i = 0; i < messages.length(); i ++) {
//                Message message = new Message(messages.getJSONObject(i));
//                msgList.add(message);
//            }
//            return msgList;
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public String getMessages() {return data.optString("messages");}

    private CarModel setPost() {
        try {
            post = new CarModel(data.getJSONObject("post"));
            return post;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class Message {
        private JSONObject data;


        public Message(JSONObject data) {
            this.data = data;
        }

        public String getMessage() { return data.optString("msg");}
        public String getSender() { return data.optString("sender");}
    }
}
