package com.remitty.caronz.messages;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.Notification.Config;
import com.remitty.caronz.R;
import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.messages.adapter.ChatAdapter;
import com.remitty.caronz.models.ChatMessage;
import com.remitty.caronz.models.ChatTyping;
import com.remitty.caronz.models.blockUserModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

public class ChatFragment extends Fragment implements View.OnClickListener {

    ArrayList<ChatMessage> chatlist = new ArrayList<>();
    ChatAdapter chatAdapter;
    ListView msgListView;
    int nextPage = 1;
    boolean hasNextPage = false;
    String adId, senderId, receiverId, type, is_Block;
    SettingsMain settingsMain;
    TextView adName, adPrice, adDate, tv_typing;
    Button block_button;
    SwipeRefreshLayout swipeRefreshLayout;
    RestService restService;
    ChatTyping chatTypingModel;
    String userId;
    boolean typingStarted;
    int totalCount;
    //    FirebaseDatabase database, database2;
//    DatabaseReference myRef, myRef2;
    long delay = 10000; // 1 seconds after user stops typing
    long last_text_edit = 0;
    Handler handler = new Handler();
    String userName;
    String typingreceiverId;
    String typingText;
    TextView tv_chatTtile, tv_online;
    //    Boolean is_BlockBool;
    private EditText msg_edittext;
    TextView BlockedTextMessage;
    LinearLayout form, MessageContainer, Blocklayout;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<blockUserModel> blockUserModelArrayList = new ArrayList<>();
    int position;
    String blcokstoreText;
    ImageButton sendButton;
    View mView;
    String lastMsg, topic;
    MyReceiver receiver;
    String chatId;
    //Boolean Is_Blocked;
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_chat_layout, container, false);
        settingsMain = new SettingsMain(getActivity());

//        database = FirebaseDatabase.getInstance();
//        myRef = database.getReference("chatTyping");

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            chatId = bundle.getString("chatId", "0");
            receiverId = bundle.getString("receiverId", "0");
            lastMsg = bundle.getString("last_msg");
            topic = bundle.getString("topic");
            String messages = bundle.getString("messages");
//            type = bundle.getBoolean("type");
            if(!messages.isEmpty()) {
                try {
                    JSONArray msgList = new JSONArray(messages);
                    for (int i = 0; i < msgList.length(); i ++) {
                        JSONObject msg = msgList.getJSONObject(i);
                        ChatMessage item = new ChatMessage();
                        item.setBody(msg.optString("msg"));
                                item.setDate(msg.optString("date"));
                        if(msg.getInt("sender") == settingsMain.getUserId()) {
                            item.setMine(true);
                            item.setImage(settingsMain.getUserImage());
                            item.setName("You");
                        }
                        else {
                            item.setMine(false);
                            item.setImage(bundle.getString("other_profile"));
                            item.setName(bundle.getString("other_name"));
                        }
//                        item.setMine(bundle.getBoolean("type"));
                        chatlist.add(item);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            receiverId = bundle.getString("receiverId", "0");
//            type = bundle.getString("type", "0");
//            is_Block = bundle.getString("is_block", "");
        }
//        Blocklayout=view.findViewById(R.id.blocklayout);
//        form=view.findViewById(R.id.form);
        initComponents();

        initListeners();

//        block_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(getMainColor())));

//        if(Block.getVisibility()==View.VISIBLE){
//            UnBlock.setVisibility(View.INVISIBLE);
//        }


//
//        if(getValue()!=null){
//        Block.setText(getValue());
//        }

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.fieldradius);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.parseColor(getMainColor()));
        sendButton.setBackground(wrappedDrawable);
/*        Drawable drawable = getResources().getDrawable(R.drawable.fieldradius).mutate();

        sendButton.setColorFilter(Color.parseColor(SettingsMain.getMainColor()), PorterDuff.Mode.SRC_IN);
sendButton.setBackground(drawable);*/
//        sendButton.setBackgroundColor(Color.parseColor(getMainColor()));
        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);
        chatAdapter = new ChatAdapter(getActivity(), chatlist);
        msgListView.setAdapter(chatAdapter);

        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

//        getChat();

//        typingIndicatoor();
//        checkLogin();
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                tv_online.setVisibility(View.GONE);
//                checkLogin();
//            }
//        }, 10000);
        return mView;
    }

    private void initListeners() {
        adName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                intent.putExtra("adId", adId);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
            }
        });

        block_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (block_button.getText().toString().equalsIgnoreCase("Block User")) {
                    BlockChat();

                } else {


//                    UnBlockChat();

                }
            }

        });

        sendButton.setOnClickListener(this);

//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                if (hasNextPage) {
//                    swipeRefreshLayout.setRefreshing(true);
//                    msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
//                    msgListView.setStackFromBottom(false);
//                    loadMore(nextPage);
//                } else {
//                    swipeRefreshLayout.setRefreshing(false);
//                }
//            }
//        });

        IntentFilter filter = new IntentFilter(Config.PUSH_NOTIFICATION);
        receiver = new MyReceiver();
        getActivity().registerReceiver(receiver, filter);
    }

    private void initComponents() {
        BlockedTextMessage = mView.findViewById(R.id.BlockedTextMessage);
        MessageContainer = mView.findViewById(R.id.messageContainer);
        adDate = mView.findViewById(R.id.verified);
        adName = mView.findViewById(R.id.loginTime);
        adPrice = mView.findViewById(R.id.text_viewName);
        adPrice.setText(topic);

        tv_typing = mView.findViewById(R.id.tv_typing);
        block_button = mView.findViewById(R.id.block_btn);
        block_button.setBackgroundColor(Color.parseColor(getMainColor()));

        swipeRefreshLayout = mView.findViewById(R.id.swipe_refresh_layout);

        msg_edittext = mView.findViewById(R.id.messageEditText);
        msgListView = mView.findViewById(R.id.msgListView);
        tv_chatTtile = getActivity().findViewById(R.id.tv_chatTtile);
        tv_online = getActivity().findViewById(R.id.tv_online);
        sendButton = mView.findViewById(R.id.sendMessageButton);
    }

    private String getValue() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString("VAL", null);
    }

    private void BlockChat() {

    }

    private void closeChat(){
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            JsonObject params = new JsonObject();
            params.addProperty("channel", chatId);

            Log.d("close chat", "" + params.toString());

            Call<ResponseBody> myCall = restService.chatClose(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    Log.d("close chat res", "" + responseObj.toString());
                    if (responseObj.isSuccessful()) {

                    }
                    SettingsMain.hideDilog();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info sendMessage", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info sendMessage error", String.valueOf(t));
                        Log.d("info sendMessage error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMore(int nextPag) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {

            JsonObject params = new JsonObject();
            params.addProperty("ad_id", adId);
            params.addProperty("sender_id", senderId);
            params.addProperty("receiver_id", receiverId);
            params.addProperty("type", type);
//            params.addProperty("is_block", is_Block);

            params.addProperty("page_number", nextPag);

            Log.d("info LoadMore Chat", "" + params.toString());

            Call<ResponseBody> myCall = restService.postGetChatORLoadMore(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info LoadChat Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info LoadChat object", "" + response.getJSONObject("data"));

                                JSONObject jsonObjectPagination = response.getJSONObject("data").getJSONObject("pagination");

                                nextPage = jsonObjectPagination.getInt("next_page");
                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");

                                JSONArray jsonArray = (response.getJSONObject("data").getJSONArray("chat"));

                                Collections.reverse(chatlist);

                                try {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        ChatMessage item = new ChatMessage();
                                        item.setImage(jsonArray.getJSONObject(i).getString("img"));
                                        item.setBody(jsonArray.getJSONObject(i).getString("text"));
                                        item.setDate(jsonArray.getJSONObject(i).getString("date"));
                                        item.setMine(jsonArray.getJSONObject(i).getString("type").equals("message"));

                                        chatlist.add(item);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Collections.reverse(chatlist);
                                msgListView.setAdapter(chatAdapter);
                                chatAdapter.notifyDataSetChanged();

                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    SettingsMain.hideDilog();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info LoadChat Excptn ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info LoadChat error", String.valueOf(t));
                        Log.d("info LoadChat error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public void sendTextMessage() {

        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        String message = msg_edittext.getText().toString();
        if (!message.equalsIgnoreCase("")) {

            if (SettingsMain.isConnectingToInternet(getActivity())) {

//                SettingsMain.showDilog(getActivity());

                JsonObject params = new JsonObject();
//                params.addProperty("ad_id", adId);
                params.addProperty("channel", chatId);
//                params.addProperty("receiver", senderId);
                params.addProperty("receiver_id", receiverId);
//                params.addProperty("type", type);
                params.addProperty("message", message);
//                params.addProperty("is_block", is_Block);
                Date currentTime = Calendar.getInstance().getTime();
                params.addProperty("date",  (currentTime.getYear()+1900) + "-"+(currentTime.getMonth()+1) + "-"+currentTime.getDate()+" "+currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds());

                Log.d("info sendMessage Object", "" + params.toString());

                Call<ResponseBody> myCall = restService.postSendMessage(params, UrlController.AddHeaders(getActivity()));
                myCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                        try {
                                Log.d("info sendMessage Resp", "" + responseObj.toString());
                            if (responseObj.isSuccessful()) {

                                JSONObject response = new JSONObject(responseObj.body().string());
                                if (response.getBoolean("success")) {
                                    ChatMessage item = new ChatMessage();
                                    item.setImage(settingsMain.getUserImage());
                                    item.setName("You");
                                    item.setBody(message);
                                    Date currentTime = Calendar.getInstance().getTime();
                                    item.setDate(currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds());
                                    item.setMine(true);
                                    chatlist.add(item);
                                    chatAdapter.notifyDataSetChanged();
                                    msg_edittext.setText("");
                                } else {
                                    Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), responseObj.errorBody().string(), Toast.LENGTH_SHORT).show();
                            }
                            SettingsMain.hideDilog();
                        } catch (JSONException e) {
                            SettingsMain.hideDilog();
                            e.printStackTrace();
                        } catch (IOException e) {
                            SettingsMain.hideDilog();
                            e.printStackTrace();
                        }
                        SettingsMain.hideDilog();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        if (t instanceof TimeoutException) {
                            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                            SettingsMain.hideDilog();
                        }
                        if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                            Log.d("info sendMessage", "NullPointert Exception" + t.getLocalizedMessage());
                            SettingsMain.hideDilog();
                        } else {
                            SettingsMain.hideDilog();
                            Log.d("info sendMessage error", String.valueOf(t));
                            Log.d("info sendMessage error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                        }
                    }
                });
            } else {
                SettingsMain.hideDilog();
                Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
            }


        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sendMessageButton) {

//            if(is_Block)
//                Toast.makeText(getActivity(),"Blocked ha ",Toast.LENGTH_SHORT).show();
//            else
            sendTextMessage();

        }
    }


    @Override
    public void onResume() {
//        closeChat();
        super.onResume();
        // register GCM registration complete receiver
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
//                new IntentFilter(Config.REGISTRATION_COMPLETE));
//
//        // register new push message receiver
//        // by doing this, the activity will be notified each time a new message arrives
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
//                new IntentFilter(Config.PUSH_NOTIFICATION));

    }

    @Override
    public void onStop(){
        super.onStop();
    }

    private void saveValue(String value) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("VAL", value).apply();

    }

    @Override
    public void onPause() {
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
        getActivity().unregisterReceiver(receiver);
        closeChat();
        super.onPause();
    }

    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
            String img = intent.getStringExtra("img");
            String text = intent.getStringExtra("text");
            String name = intent.getStringExtra("name");
            String channel = intent.getStringExtra("channel");
            senderId = intent.getStringExtra("sender");

            Log.d("chat channel", channel);
            Log.d("chat senderId", senderId);
            Log.d("chat chatId", chatId);

            if(chatId.equals(channel)) {
                ChatMessage item = new ChatMessage();
                item.setImage(img);
                item.setName(name);
                item.setBody(text);
                Date currentTime = Calendar.getInstance().getTime();
                item.setDate(currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds());
                item.setMine(false);
                chatlist.add(item);
                chatAdapter.notifyDataSetChanged();
            }
        }
    }
}
