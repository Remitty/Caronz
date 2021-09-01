package com.remitty.caronz.car_detail;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.adapters.FeedbackAdapter;
import com.remitty.caronz.messages.ChatActivity;
import com.remitty.caronz.models.CarFeedback;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.models.ReceivedMessageModel;
import com.remitty.caronz.gms.CustomGooglePlacesSearchActivity;
import com.remitty.caronz.payment.HireBookingActivity;
import com.remitty.caronz.payment.RentalBookingActivity;
import com.remitty.caronz.payment.StripePayment;
import com.remitty.caronz.profile.ProfileActivity;
import com.google.gson.JsonObject;


import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.phonenumberui.utility.Utility;

import ss.com.bannerslider.banners.Banner;
import ss.com.bannerslider.banners.RemoteBanner;
import ss.com.bannerslider.views.BannerSlider;

public class FragmentCarDetail extends Fragment implements Serializable, RuntimePermissionHelper.permissionInterface {
    public static String myId, myBookId;

    View mView;
    CardView cardRent, cardSale, cardRating, cardHire;
    NestedScrollView nestedScroll;
    Dialog callDialog;
    Dialog dialog;
    SettingsMain settingsMain;
    RestService restService, authRestService;
    RuntimePermissionHelper runtimePermissionHelper;

    TextView textViewAdName, tvCatName, textViewDescrition, tvRentPrice, tvSalePrice, tvRentTotalPrice, tvSpeed, tvSeat, tvViewComments, tvCarLocation, tvCarRate, tvCarTransmission, tvHirePrice;
    LinearLayout contactLayout;
    TextView btnMsg;
    Button btnBuy, btnBook, btnHire;
    AppCompatEditText editPickup;
    HtmlTextView htmlTextView;
    RatingBar ratingBar;
    ImageButton btnViewSeller;

    BannerSlider bannerSlider;
    List<Banner> banners = new ArrayList<>();
    ArrayList<String> imageUrls = new ArrayList<>();

    RecyclerView mRelatedRecyclerView;
    ArrayList<CarModel> relatedList = new ArrayList<>();

    RecyclerView mCommentsRecyclerView;
    FeedbackAdapter feedbackAdapter;
    ArrayList<CarFeedback> commentsList = new ArrayList<>();

    CarModel item;

    private String phoneNumber, maskedPhoneNumber;

    public FragmentCarDetail() {
        // Required empty public constructor
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_ad_detail, container, false);

        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            myId = bundle.getString("id", "0");
            myBookId = bundle.getString("book_id");
        }

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class);
        authRestService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());
        runtimePermissionHelper = new RuntimePermissionHelper(getActivity(), this);

        initComponents();

        initListeners();

        Utility.hideSoftKeyboard(getActivity());

        mRelatedRecyclerView = mView.findViewById(R.id.related_recycler_view);
        mRelatedRecyclerView.setHasFixedSize(true);
        GridLayoutManager MyLayoutManager2 = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRelatedRecyclerView.setLayoutManager(MyLayoutManager2);
//        ItemMainHomeRelatedAdapter adapter = new ItemMainHomeRelatedAdapter(getActivity(), relatedList);
//        mRelatedRecyclerView.setAdapter(adapter);
//        adapter.setOnItemClickListener(new OnItemClickListener2() {
//            @Override
//            public void onItemClick(RentalModel item) {
//                Log.d("item_id", item.getId());
//                FragmentCarDetail.myId = item.getId();
//                recreateAdDetail();
//                nestedScroll.scrollTo(0, 0);
//            }
//        });

        mCommentsRecyclerView = mView.findViewById(R.id.rating_recycler_view);
        mCommentsRecyclerView.setHasFixedSize(true);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        feedbackAdapter = new FeedbackAdapter(getActivity(), commentsList);
        mCommentsRecyclerView.setAdapter(feedbackAdapter);


        getAllData(myId);

        return mView;
    }

    private void initListeners() {


        btnMsg.setOnClickListener(view12 -> {
                if (!settingsMain.getAppOpen()) {
                    Toast.makeText(getActivity(), settingsMain.getNoLoginMessage(), Toast.LENGTH_SHORT).show();

                } else {
                     showDilogMessage();
                }
            });

        btnBook.setOnClickListener(view14 -> {
            if (!settingsMain.getAppOpen()) {
                Toast.makeText(getActivity(), settingsMain.getNoLoginMessage(), Toast.LENGTH_SHORT).show();

            } else {
                Intent intent = new Intent(getActivity(), RentalBookingActivity.class);
                intent.putExtra("car_id", item.getId());
                startActivity(intent);
            }
        });

        btnHire.setOnClickListener(view18 -> {
            Intent intent = new Intent(getActivity(), HireBookingActivity.class);
            intent.putExtra("car_id", item.getId());
            startActivity(intent);
        });

        btnBuy.setOnClickListener(view15 -> {
            if (!settingsMain.getAppOpen()) {
                Toast.makeText(getActivity(), settingsMain.getNoLoginMessage(), Toast.LENGTH_SHORT).show();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to to buy this car?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), StripePayment.class);
                        intent.putExtra("id", item.getId());
                        intent.putExtra("service", "buy");
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        tvViewComments.setOnClickListener(view16 -> {
            Intent intent = new Intent(getActivity(), FeedbackActivity.class);
            intent.putExtra("id", myId);
            startActivity(intent);
        });

        bannerSlider.setOnBannerClickListener(position -> {
            if (banners.size() > 0) {

//                Intent i = new Intent(getActivity(), FullScreenViewActivity.class);
//                i.putExtra("imageUrls", imageUrls);
//                i.putExtra("position", position);
//                startActivity(i);
            }
        });

        editPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyBoardFromView(getActivity());
            }
        });

        btnViewSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("userId", item.getSellerId());
                startActivity(intent);
            }
        });
    }

    private void initComponents() {


        nestedScroll = mView.findViewById(R.id.scrollViewUp);
        bannerSlider = mView.findViewById(R.id.banner_slider1);

        btnMsg = getActivity().findViewById(R.id.message);
        contactLayout = getActivity().findViewById(R.id.contact_layout);

        textViewAdName = mView.findViewById(R.id.tv_car_name);
        tvCatName = mView.findViewById(R.id.tv_car_cat);
        textViewDescrition = mView.findViewById(R.id.tv_car_description);
        tvRentPrice = mView.findViewById(R.id.tv_rent_price);
        tvRentTotalPrice = mView.findViewById(R.id.tv_car_rent_total_price);
        tvSalePrice = mView.findViewById(R.id.tv_car_sale_price);
        tvHirePrice = mView.findViewById(R.id.tv_hire_price);
        tvSpeed = mView.findViewById(R.id.tv_car_speed);
        tvSeat = mView.findViewById(R.id.tv_car_seat);
        tvViewComments = mView.findViewById(R.id.tv_more_rating);
        tvCarLocation = mView.findViewById(R.id.tv_car_location);
        tvCarRate = mView.findViewById(R.id.tv_car_rate);
        tvCarTransmission = mView.findViewById(R.id.tv_car_transmission);

        btnBook = mView.findViewById(R.id.btn_rent);
        btnBuy = mView.findViewById(R.id.btn_buy);
        btnHire = mView.findViewById(R.id.btn_hire);
        btnViewSeller = mView.findViewById(R.id.btn_view_seller);

        cardRent = mView.findViewById(R.id.card_rent);
        cardSale = mView.findViewById(R.id.card_sale);
        cardRating = mView.findViewById(R.id.card_rate);
        cardHire = mView.findViewById(R.id.card_hire);

        editPickup = mView.findViewById(R.id.edit_pickup);
//        editPickup.setFocusable(true);

        htmlTextView = mView.findViewById(R.id.html_text);
        ratingBar = mView.findViewById(R.id.car_rate);

        LayerDrawable stars = (LayerDrawable) this.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);

    }

    private void getAllData(final String myId) {
        this.myId = myId;
        nestedScroll.scrollTo(0, 0);
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("car_id", myId);
            if(myBookId != null){
                params.addProperty("book_id", myBookId);
            }

            Log.d("info send AdDetails", "" + params.toString());

            Call<ResponseBody> myCall = restService.getAdsDetail(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info AdDetails Respon", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if (response.getBoolean("success")) {
                                Log.d("info AdDetails object", "" + response.getJSONObject("data"));


                                setAllViewsText(response.getJSONObject("data"));

                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
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
                    SettingsMain.hideDilog();
                    Log.d("info AdDetails error", String.valueOf(t));
                    Log.d("info AdDetails error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    private void setAllViewsText(final JSONObject data) {

            item = new CarModel(data);
        try {


            textViewAdName.setText(item.getName());
            tvCatName.setText(item.getCatName());
            textViewDescrition.setText(item.getDescription());
            tvSeat.setText(item.getSeats());
            tvSpeed.setText(item.getDistance());
            tvCarLocation.setText(item.getLocation());
            tvRentPrice.setText("$ " + item.getPrice());
            tvHirePrice.setText("$ " + item.getPrice());
            tvRentTotalPrice.setText("$ 0");
            tvSalePrice.setText("$ " + item.getPrice());
            ratingBar.setRating(item.getRate());
            tvCarRate.setText(String.valueOf(item.getRate()));
            tvCarTransmission.setText(item.getTransmission());

            banners.clear();
            imageUrls.clear();
            relatedList.clear();
            if(bannerSlider != null)
            bannerSlider.removeAllBanners();

            for (int i = 0; i < item.getImages().length(); i++) {
                String path =item.getImages().getJSONObject(i).getString("thumb");
                if(!path.startsWith("http"))
                    path = UrlController.ASSET_ADDRESS + path;
                banners.add(new RemoteBanner(path));
                imageUrls.add(path);
                banners.get(i).setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            if (banners.size() > 0 && bannerSlider != null)
                bannerSlider.setBanners(banners);

            if(!item.isRental()) cardRent.setVisibility(View.GONE);
            if(!item.isBuy()) cardSale.setVisibility(View.GONE);
            if(!item.isHire()) cardHire.setVisibility(View.GONE);

            if(item.isBuy()) cardRating.setVisibility(View.GONE);

            if(settingsMain.getAppOpen())
            if(settingsMain.getUserId() == item.getSellerId()) {
                cardRent.setVisibility(View.GONE);
                cardSale.setVisibility(View.GONE);
                cardHire.setVisibility(View.GONE);
                contactLayout.setVisibility(View.GONE);
            }

            commentsList = item.getFeedbacks();
            feedbackAdapter.notifyDataSetChanged();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void showDilogMessage() {
        dialog = new Dialog(getActivity(), R.style.customDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_message);
        //noinspection ConstantConditions
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.parseColor("#00000000")));

        Button Send = dialog.findViewById(R.id.send_button);
        Button Cancel = dialog.findViewById(R.id.cancel_button);

        final EditText message = dialog.findViewById(R.id.editText3);

        Send.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
        Cancel.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        Send.setText("Send");
        message.setHint("Please type");
        Cancel.setText("Cancel");

        Send.setOnClickListener(v -> {

            if (!message.getText().toString().isEmpty()) {
                sendMessage(message.getText().toString());

                dialog.dismiss();
            } else
                message.setError("");
        });

        Cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    void sendMessage(String msg) {

        if (SettingsMain.isConnectingToInternet(getActivity())) {
            SettingsMain.showDilog(getActivity());

            JsonObject params = new JsonObject();
            params.addProperty("car_id", myId);
            params.addProperty("message", msg);
            Date currentTime = Calendar.getInstance().getTime();
            params.addProperty("date",  (currentTime.getYear()+1900) + "-"+(currentTime.getMonth()+1) + "-"+currentTime.getDate()+" "+currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds());
            Log.d("info sendMeassage", myId);

            Call<ResponseBody> myCall = authRestService.postSendMessageFromAd(params, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info sendMeassage Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if (response.getBoolean("success")) {
                                ReceivedMessageModel message = new ReceivedMessageModel();
                                message.setData(response.getJSONObject("data"));
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("chatId", message.getId());
                                intent.putExtra("topic", item.getName());
                                intent.putExtra("receiverId", message.getReceiver_id());
                                intent.putExtra("last_msg", message.getLastMessage());
                                intent.putExtra("messages", message.getMessages());
                                intent.putExtra("other_name", message.getOtherName());
                                intent.putExtra("other_profile", message.getOtherProfile());
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                            SettingsMain.hideDilog();
                        }
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
                    SettingsMain.hideDilog();
                    Log.d("info sendMeassage error", String.valueOf(t));
                    Log.d("info sendMeassage error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void recreateAdDetail() {
        FragmentCarDetail fragmentAdDetail = new FragmentCarDetail();
        Bundle bundle = new Bundle();
        bundle.putString("id", myId);
        fragmentAdDetail.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, fragmentAdDetail);
        transaction.commit();
    }

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    public void Call() {
        if (callDialog != null) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + maskedPhoneNumber));
            startActivity(intent);
            callDialog.dismiss();
        }
    }

    @Override
    public void onSuccessPermission(int code) {

        Call();
    }


}
