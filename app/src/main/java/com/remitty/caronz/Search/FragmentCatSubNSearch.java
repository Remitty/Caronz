package com.remitty.caronz.Search;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.remitty.caronz.R;
import com.remitty.caronz.Search.adapter.ItemCatgorySubListAdapter;
import com.remitty.caronz.adapters.ItemSearchFeatureAdsAdapter;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.models.RentalModel;
import com.remitty.caronz.utills.NestedScroll;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

public class FragmentCatSubNSearch extends Fragment {

    public static String requestFrom = "";
    ArrayList<RentalModel> searchedAdList = new ArrayList<>();
    ArrayList<RentalModel> featureAdsList = new ArrayList<>();
    RecyclerView MyRecyclerView, recyclerViewFeatured;
    //EditText editTextSearch;
    SettingsMain settingsMain;
    TextView textViewTitleFeature, textViewFilterText;
    ItemCatgorySubListAdapter itemCatgorySubListAdapter;
    ItemSearchFeatureAdsAdapter itemSearchFeatureAdsAdapter;
    LinearLayout viewProductLayout;
    JSONObject jsonObjectPagination;
    JsonObject lastSentParamas;
    LinearLayout linearLayoutCollapse;
    Spinner spinnerFilter;
//    Button spinnerFilterText;
    LinearLayout linearLayoutFilter, emptyLayout, featuredLayout, searchLayout;
    boolean isSort = false;
    RelativeLayout relativeLayoutSpiner;
    NestedScrollView scrollView;
    boolean loading = true, hasNextPage = false;
    ProgressBar progressBar;
    int currentPage = 1, nextPage = 1, totalPage = 0;
    String myId, title, ad_country, nearby_latitude, nearby_longitude, nearby_distance, ad_cats1, city;
    RestService restService;
    LinearLayoutManager MyLayoutManager2;
    private JSONObject jsonObjectFilterSpinner;
    private boolean spinnerTouched2 = false;

    public FragmentCatSubNSearch() {
        // Required empty public constructor
    }

    public void adforest_recylerview_autoScroll(final int duration, final int pixelsToMove, final int delayMillis,
                                                final RecyclerView recyclerView, final LinearLayoutManager gridLayoutManager
            , final ItemSearchFeatureAdsAdapter adapter) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int count = 0;
            boolean flag = true;

            @Override
            public void run() {
                try {
                    if (count < adapter.getItemCount()) {
                        if (count == adapter.getItemCount() - 1) {
                            flag = false;
                        } else if (count == 0) {
                            flag = true;
                        }
                        if (flag) count++;
                        else count--;

                        recyclerView.smoothScrollToPosition(count);
                        handler.postDelayed(this, duration);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }
        };

        handler.postDelayed(runnable, delayMillis);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cat_subcatlist, container, false);
        scrollView = view.findViewById(R.id.scrollView);
        progressBar = view.findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            myId = bundle.getString("id", "");
            title = bundle.getString("title", "");
            ad_country = bundle.getString("ad_country", "");
            nearby_latitude = bundle.getString("nearby_latitude", "");
            nearby_longitude = bundle.getString("nearby_longitude", "");
            nearby_distance = bundle.getString("nearby_distance", "");
            ad_cats1 = bundle.getString("ad_cats1", "");
            city = bundle.getString("city", "");
        }
        settingsMain = new SettingsMain(getActivity());

        linearLayoutCollapse = view.findViewById(R.id.linearLayout);
        linearLayoutFilter = view.findViewById(R.id.filter_layout);
        textViewFilterText = view.findViewById(R.id.textViewFilter);
        spinnerFilter = view.findViewById(R.id.spinner);
//        spinnerFilterText=view.findViewById(R.id.spinnerafter);
//        spinnerFilterText.setBackgroundColor(Color.parseColor(SettingsMain.getMainColor()));
        relativeLayoutSpiner = view.findViewById(R.id.rel1);

//        viewProductLayout = view.findViewById(R.id.customOptionLL);

        textViewTitleFeature = view.findViewById(R.id.textView6);
///////search products bt property
        MyRecyclerView = view.findViewById(R.id.recycler_view);
        MyRecyclerView.setHasFixedSize(true);
        MyRecyclerView.setNestedScrollingEnabled(false);
        MyRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        emptyLayout = view.findViewById(R.id.empty_view);
        featuredLayout = view.findViewById(R.id.ll_featured);
        searchLayout = view.findViewById(R.id.search_realty);

//        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
//        MyLayoutManager.setOrientation(RecyclerView.VERTICAL);
//        MyRecyclerView.setLayoutManager(MyLayoutManager);
///////search featured listings
        recyclerViewFeatured = view.findViewById(R.id.recycler_view2);
        recyclerViewFeatured.setHasFixedSize(true);
        MyLayoutManager2 = new LinearLayoutManager(getActivity());
        MyLayoutManager2.setInitialPrefetchItemCount(3);
        MyLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFeatured.setLayoutManager(MyLayoutManager2);

        restService = UrlController.createService(RestService.class);
//        if (!settingsMain.getAppOpen()) {
//        } else
//            restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

        scrollView.setOnScrollChangeListener(new NestedScroll() {
            @Override
            public void onScroll() {
                if (loading) {
                    loading = false;
                    Log.d("info data object", "sdfasdfadsasdfasdfasdf");

                    if (hasNextPage) {
                        progressBar.setVisibility(View.VISIBLE);
                        adforest_loadmore();
                    }
                }
            }
        });

//        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
//                        scrollY > oldScrollY) {
//
//                    if (nextPage <= totalPage) {
//                        progressBar.setVisibility(View.VISIBLE);
//                        adforest_loadmore();
//
//                        Log.d("heeeeeeelo", nextPage + "==" + totalPage);
//
//                    } else {
//                        progressBar.setVisibility(View.GONE);
//                    }
//                }
//            }
//        });
        getActivity().setTitle("Search");
        adforest_submitQuery("");

        if (requestFrom.equals("")) {
            ((HomeActivity) getActivity()).updateApi(new HomeActivity.UpdateFragment() {
                @Override
                public void updatefrag(String s) {
                    title = s;
                    adforest_submitQuery("");
                    scrollView.scrollTo(0, 0);

                }

                @Override
                public void updatefrag(String latitude, String longitude, String distance) {
                    nearby_latitude = latitude;
                    nearby_longitude = longitude;
                    nearby_distance = distance;
                    adforest_submitQuery("");
                }
            });
        }
        if (requestFrom.equals("")) {
            SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setEnabled(true);
        }
        return view;
    }

    private void adforest_submitQuery(String s) {

        JsonObject params = new JsonObject();
        if (!myId.equals("")) {
            params.addProperty("catId", myId);
        }
        if (!title.equals("")) {
            params.addProperty("title", title);
        }
        if (isSort) {
            params.addProperty("sort", s);
        }
        if (!ad_country.equals("")) {
            params.addProperty("ad_country", ad_country);
        }
        if (!nearby_latitude.equals("") && !nearby_longitude.equals("") && !nearby_distance.equals("0") && !nearby_distance.equals("")) {
            params.addProperty("nearby_latitude", nearby_latitude);
            params.addProperty("nearby_longitude", nearby_longitude);
            params.addProperty("nearby_distance", nearby_distance);
        }
        if(!city.equals("")){
            params.addProperty("city", city);
        }
        if (!ad_cats1.equals("")) {
            params.addProperty("ad_cats1", ad_cats1);
        }
        lastSentParamas = params;

        params.addProperty("page_number", 1);


//        if (SettingsMain.isConnectingToInternet(getActivity())) {
//
//            if (!HomeActivity.checkLoading)
//                SettingsMain.showDilog(getActivity());
//            Log.d("info Send MenuSearch =", "" + params.toString());
//
//            Call<ResponseBody> myCall = restService.postGetMenuSearchData(params, UrlController.AddHeaders(getActivity()));
//            myCall.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
//                    try {
//                            Log.d("info MenuSearch Resp", "" + responseObj.toString());
//                        if (responseObj.isSuccessful()) {
//
//                            JSONObject response = new JSONObject(responseObj.body().string());
//                            if (response.getBoolean("success")) {
//                                Log.d("info MenuSearch object", "" + response.getJSONObject("data"));
//                                HomeActivity.checkLoading = false;
//
////                                getActivity().setTitle(response.getJSONObject("extra").getString("title"));
//
////                                if (response.getJSONObject("extra").getBoolean("is_show_featured")) {
////                                    textViewTitleFeature.setVisibility(View.VISIBLE);
////                                } else {
////                                    textViewTitleFeature.setVisibility(View.GONE);
////                                }//for test
////                                spinnerFilterText.setOnClickListener(new View.OnClickListener() {
////                                    @Override
////                                    public void onClick(View view) {
////                                        Intent i = new Intent(getActivity(), SearchActivity.class);
//////                                        view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.zoomin));
////                                        startActivity(i);
////
////                                    }
////                                });
////
//
//                                try {
//
//                                    jsonObjectFilterSpinner = response.getJSONObject("topbar");
//                                    final JSONArray dropDownJSONOpt = jsonObjectFilterSpinner.getJSONArray("sort_arr");
//                                    final ArrayList<String> SpinnerOptions;
//                                    SpinnerOptions = new ArrayList<>();
//                                    for (int j = 0; j < dropDownJSONOpt.length(); j++) {
//                                        SpinnerOptions.add(dropDownJSONOpt.getJSONObject(j).getString("value"));
//                                    }
//
//                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_medium, SpinnerOptions);
//                                    spinnerFilter.setAdapter(adapter);
//
//                                    spinnerFilter.setOnTouchListener((v, event) -> {
//                                        System.out.println("Real touch felt.");
//                                        spinnerTouched2 = true;
//                                        return false;
//                                    });
//
//                                    spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                                        @Override
//                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//                                            if (spinnerTouched2) {
//                                                try {
//                                                    //Toast.makeText(getActivity(), "" + dropDownJSONOpt.getJSONObject(i).getString("key"), Toast.LENGTH_SHORT).show();
//                                                    isSort = true;
//                                                    adforest_submitQuery(dropDownJSONOpt.getJSONObject(i).getString("key"));
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                            spinnerTouched2 = false;
//                                        }
//
//                                        @Override
//                                        public void onNothingSelected(AdapterView<?> adapterView) {
//
//                                        }
//                                    });
//
//                                }
//                                catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//
//
////                                jsonObjectPagination = response.getJSONObject("pagination");
////
////                                nextPage = jsonObjectPagination.getInt("next_page");
////                                currentPage = jsonObjectPagination.getInt("current_page");
////                                totalPage = jsonObjectPagination.getInt("max_num_pages");
////                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");
//                                loading = true;
//
//                                adforest_loadList(response.getJSONObject("data").getJSONObject("featured"),
//                                        response.getJSONObject("data").getJSONArray("products"),
//                                        response.getJSONObject("topbar"));
//
//                                itemCatgorySubListAdapter = new ItemCatgorySubListAdapter(getActivity(), searchedAdList);
//                                itemSearchFeatureAdsAdapter = new ItemSearchFeatureAdsAdapter(getActivity(), featureAdsList);
//
//                                recyclerViewFeatured.setAdapter(itemSearchFeatureAdsAdapter);
//
//                                if (settingsMain.isFeaturedScrollEnable()) {
//
//                                    adforest_recylerview_autoScroll(settingsMain.getFeaturedScroolDuration(),
//                                            40, settingsMain.getFeaturedScroolLoop(),
//                                            recyclerViewFeatured, MyLayoutManager2, itemSearchFeatureAdsAdapter);
//                                }
//                                MyRecyclerView.setAdapter(itemCatgorySubListAdapter);
//
//                                itemSearchFeatureAdsAdapter.setOnItemClickListener(new CatSubCatOnclicklinstener() {
//                                    @Override
//                                    public void onItemClick(RentalModel item) {
//                                        Intent intent = new Intent(getActivity(), CarDetailActivity.class);
//                                        intent.putExtra("adId", item.getId());
//                                        getActivity().startActivity(intent);
//                                        getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
//                                    }
//
//                                    @Override
//                                    public void onItemTouch(RentalModel item) {
//
//                                    }
//
//                                    @Override
//                                    public void addToFavClick(View v, String position) {
//                                    }
//                                });
//
//                                itemCatgorySubListAdapter.setOnItemClickListener(new CatSubCatOnclicklinstener() {
//                                    @Override
//                                    public void onItemClick(RentalModel item) {
//
//                                        Intent intent = new Intent(getActivity(), CarDetailActivity.class);
//                                        intent.putExtra("adId", item.getId());
//                                        getActivity().startActivity(intent);
//                                        getActivity().overridePendingTransition(R.anim.right_enter, R.anim.left_out);
//                                    }
//
//                                    @Override
//                                    public void onItemTouch(RentalModel item) {
//
//                                    }
//
//                                    @Override
//                                    public void addToFavClick(View v, String position) {
//
//                                    }
//                                });
//                            } else {
//                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        SettingsMain.hideDilog();
//                    } catch (JSONException e) {
//                        SettingsMain.hideDilog();
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        SettingsMain.hideDilog();
//                        e.printStackTrace();
//                    }
//                    SettingsMain.hideDilog();
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    if (t instanceof TimeoutException) {
//                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
//                        settingsMain.hideDilog();
//                    }
//                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
//
//                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
//                        settingsMain.hideDilog();
//                    }
//                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
//                        Log.d("info MenuSearch ", "NullPointert Exception" + t.getLocalizedMessage());
//                        settingsMain.hideDilog();
//                    } else {
//                        SettingsMain.hideDilog();
//                        Log.d("info MenuSearch err", String.valueOf(t));
//                        Log.d("info MenuSearch err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
//                    }
//                }
//            });
//        } else {
//            SettingsMain.hideDilog();
//            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
//        }
        isSort = false;
    }

    private void adforest_loadmore() {

        lastSentParamas.addProperty("page_number", nextPage);
        Log.d("info loadMore MenuSrch=", "" + lastSentParamas.toString());

//        if (SettingsMain.isConnectingToInternet(getActivity())) {
//            Call<ResponseBody> myCall = restService.postGetMenuSearchData(lastSentParamas, UrlController.AddHeaders(getActivity()));
//            myCall.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
//                    try {
//                        if (responseObj.isSuccessful()) {
//                            Log.d("info MenuSearch Resp", "" + responseObj.toString());
//
//                            JSONObject response = new JSONObject(responseObj.body().string());
//                            if (response.getBoolean("success")) {
//                                Log.d("info MenuSearch object", "" + response.getJSONObject("data"));
//
//                                Log.d("info extra_obj", "" + response.getJSONObject("extra"));
//
//                                jsonObjectPagination = response.getJSONObject("pagination");
//
//                                nextPage = jsonObjectPagination.getInt("next_page");
//                                currentPage = jsonObjectPagination.getInt("current_page");
//                                totalPage = jsonObjectPagination.getInt("max_num_pages");
//                                hasNextPage = jsonObjectPagination.getBoolean("has_next_page");
//
//                                JSONArray searchAds = response.getJSONObject("data").getJSONArray("ads");
//
//                                try {
//                                    Log.d("info MenuSearch is = ", searchAds.toString());
//                                    if (searchAds.length() > 0) {
//                                        for (int i = 0; i < searchAds.length(); i++) {
//
//                                            RentalModel item = new RentalModel();
//
//                                            JSONObject object = searchAds.getJSONObject(i);
//
//                                            item.setId(object.getString("ad_id"));
//                                            item.setCardName(object.getString("ad_title"));
//                                            item.setDate(object.getString("ad_date"));
//                                            item.setAdViews(object.getString("ad_views"));
//                                            item.setPath(object.getString("ad_cats_name"));
//                                            item.setPrice(object.getJSONObject("ad_price").getString("price"));
//                                            item.setImageResourceId((object.getJSONArray("images").getJSONObject(0).getString("thumb")));
//                                            item.setLocation(object.getJSONObject("location").getString("location"));
//                                            item.setIsturned(0);
//                                            item.setIs_show_countDown(object.getJSONObject("ad_timer").getBoolean("is_show"));
//                                            if (object.getJSONObject("ad_timer").getBoolean("is_show"))
//                                                item.setTimer_array(object.getJSONObject("ad_timer").getJSONArray("timer"));
//                                            searchedAdList.add(item);
//                                        }
//                                        MyRecyclerView.setAdapter(itemCatgorySubListAdapter);
//                                        itemCatgorySubListAdapter.notifyDataSetChanged();
//                                        progressBar.setVisibility(View.GONE);
//                                        loading = true;
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        SettingsMain.hideDilog();
//                    } catch (JSONException e) {
//                        SettingsMain.hideDilog();
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        SettingsMain.hideDilog();
//                        e.printStackTrace();
//                    }
//                    SettingsMain.hideDilog();
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    if (t instanceof TimeoutException) {
//                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
//                        settingsMain.hideDilog();
//                    }
//                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {
//
//                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
//                        settingsMain.hideDilog();
//                    }
//                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
//                        Log.d("info MenuSearch ", "NullPointert Exception" + t.getLocalizedMessage());
//                        settingsMain.hideDilog();
//                    } else {
//                        SettingsMain.hideDilog();
//                        Log.d("info MenuSearch err", String.valueOf(t));
//                        Log.d("info MenuSearch err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
//                    }
//                }
//            });
//        } else {
//            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
//        }
    }

    void adforest_loadList(JSONObject featureObject, JSONArray searchAds, JSONObject filtertext) {
        searchedAdList.clear();
        featureAdsList.clear();
        try {
            Log.d("search jsonaarry is = ", searchAds.toString());
            if (searchAds.length() > 0)
                for (int i = 0; i < searchAds.length(); i++) {

                    RentalModel item = new RentalModel();

                    JSONObject object = searchAds.getJSONObject(i);

                    item.setId(object.getString("id"));
                    item.setCardName(object.getString("title"));
//                    item.setDate(object.getString("ad_date"));
//                    item.setAdViews(object.getString("ad_views"));
//                    item.setPath(object.getString("ad_cats_name"));
                    item.setPrice(object.getString("unit_price"));
                    if(object.getJSONArray("images").length() > 0)
                        item.setImageResourceId(object.getJSONArray("images").getJSONObject(0).getString("thumb"));
                    item.setLocation(object.getString("location"));
                    item.setIsturned(0);
//                    item.setIs_show_countDown(object.getJSONObject("ad_timer").getBoolean("is_show"));
//                    if (object.getJSONObject("ad_timer").getBoolean("is_show"))
//                        item.setTimer_array(object.getJSONObject("ad_timer").getJSONArray("timer"));
                    searchedAdList.add(item);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.d("feature Object is = ", featureObject.getJSONArray("products").toString());
//            textViewTitleFeature.setText(featureObject.getString("text"));
            if (featureObject.getJSONArray("products").length() > 0)
                for (int i = 0; i < featureObject.getJSONArray("products").length(); i++) {

                    RentalModel item = new RentalModel();
                    JSONObject object = featureObject.getJSONArray("products").getJSONObject(i);

                    item.setId(object.getString("id"));
                    item.setCardName(object.getString("title"));
//                    item.setDate(object.getString("ad_date"));
//                    item.setAdViews(object.getString("ad_views"));
//                    item.setPath(object.getString("ad_cats_name"));
                    item.setPrice(object.getString("unit_price"));
                    if(object.getJSONArray("images").length() > 0)
                        item.setImageResourceId(object.getJSONArray("images").getJSONObject(0).getString("thumb"));
                    item.setLocation(object.getString("location"));
                    item.setIsturned(0);
//                    item.setIs_show_countDown(object.getJSONObject("ad_timer").getBoolean("is_show"));
//                    if (object.getJSONObject("ad_timer").getBoolean("is_show"))
//                        item.setTimer_array(object.getJSONObject("ad_timer").getJSONArray("timer"));

                    item.setIsturned(1);
                    featureAdsList.add(item);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            textViewFilterText.setText(filtertext.getString("category_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(featureAdsList.size() > 0 || searchedAdList.size() > 0) {
            emptyLayout.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
        else{
            emptyLayout.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        if(featureAdsList.size() > 0) {
            featuredLayout.setVisibility(View.VISIBLE);
        }
        else featuredLayout.setVisibility(View.GONE);
    }

    public void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();

        featureAdsList.clear();
        searchedAdList.clear();

        nextPage = 1;
        currentPage = 1;
        totalPage = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}