package com.remitty.caronz.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.adapters.ItemLocationAdapter;
import com.remitty.caronz.helper.GridSpacingItemDecoration;
import com.remitty.caronz.helper.ItemLocationOnclicklistener;
import com.remitty.caronz.home.helper.ChooseLocationModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

public class ChooseLocationFragment extends Fragment {
    public ChooseLocationFragment() {
        // Required empty public constructor
    }

    SettingsMain settingsMain;
    RelativeLayout relativeLayout;
    TextView headingChooseLocation;
    RecyclerView recyclerview;
    static String image, title1, title2, title3, isMultiLine, MainHeading;
    static JSONArray siteLocations;
    List<ChooseLocationModel> locationModelList = new ArrayList<>();
    RestService restService;

    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public static void setData(String title1, JSONArray jsonArray) {
        ChooseLocationFragment.title1 = title1;


        //        ChooseLanguageActivity.isMultiLine=isMultiLine;

        ChooseLocationFragment.siteLocations = jsonArray;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_layout, container, false);

        settingsMain = new SettingsMain(getActivity());
        if (settingsMain.getAppOpen()) {
            restService = UrlController.createService(RestService.class);
        } else
            restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), getActivity());
        relativeLayout = view.findViewById(R.id.location_activiy);
//        headingChooseLocation = view.findViewById(R.id.txt_choose_location);
        recyclerview = view.findViewById(R.id.recyclerview_choose_location);
//        Toolbar toolbar =view. findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        toolbar.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));
//        setTitle(title1);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.parseColor(settingsMain.getMainColor()));
//        }

        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(recyclerview, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(getActivity(), 1);
        MyLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerview.setLayoutManager(MyLayoutManager);
        int spacing = 0; // 50px
        recyclerview.addItemDecoration(new GridSpacingItemDecoration(1, spacing, false));
        adforest_setAllLocations();
        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setEnabled(false);
//        swipeRefreshLayout = view.findViewById(R.id.pullToRefresh);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                adforest_setAllLocations();
//                swipeRefreshLayout.setRefreshing(true);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//                }, 250);
//            }
//        });

        return view;
    }

    private void adforest_setAllLocations() {
        ItemLocationAdapter adapter = new ItemLocationAdapter(getActivity(), locationModelList);
        recyclerview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        locationModelList.clear();
        getActivity().setTitle(title1);
        for (int i = 0; i < siteLocations.length(); i++) {

            ChooseLocationModel chooseLocationModel = new ChooseLocationModel();
            JSONObject jsonObject = null;
            try {
                jsonObject = siteLocations.getJSONObject(i);
                chooseLocationModel.setLocationId(jsonObject.getString("location_id"));
                chooseLocationModel.setTitle(jsonObject.getString("location_name"));
                locationModelList.add(chooseLocationModel);
//                SettingsMain.hideDilog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.setItemLocationOnclicklistener(new ItemLocationOnclicklistener() {
            @Override
            public void onItemClick(ChooseLocationModel chooseLocationModel) {
//                ChooseLocationActivity.this.updateViews(chooseLocationModel.getLocationId());
                adforest_PostLocationId(chooseLocationModel.getLocationId());
//                ChooseLanguageActivity.this.updateViews(chooseLanguageModel.getLanguageCode());
//                Intent intent = new Intent(ChooseLocationActivity.this.getApplicationContext(), HomeActivity.class);
//                settingsMain.setLanguageCode(chooseLanguageModel.getLanguageCode());
//                settingsMain.setLocationId(chooseLocationModel.getLocationId());
//                settingsMain.setLocationChanged(true);
//                settingsMain.setLanguageChanged(true);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                ChooseLocationActivity.this.startActivity(intent);
//                ChooseLocationActivity.this.finish();
            }
        });
    }

    public void adforest_PostLocationId(String locationId) {
        if (SettingsMain.isConnectingToInternet(getActivity())) {
            SettingsMain.showDilog(getActivity());
            JsonObject params = new JsonObject();
            params.addProperty("location_id", locationId);
            Log.d("info post LocationId", "" + params.toString());
//            Call<ResponseBody> myCall = restService.postLocationID(params,UrlController.AddHeaders(this));
            Call<ResponseBody> mycall2 = restService.postLocationID(params, UrlController.AddHeaders(getActivity()));

            Log.d("resSErvice", restService.toString());
            mycall2.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    if (responseObj.isSuccessful()) {
                        Log.d("info location Resp", "" + responseObj.toString());
                        JSONObject response = null;
                        try {
                            response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                settingsMain.setLocationChanged(true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getActivity().startActivity(intent);
                                getActivity().finish();
                            }
                            SettingsMain.hideDilog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }

    }


    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}
