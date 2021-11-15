package com.remitty.caronz.home;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.remitty.caronz.Search.FragmentSearch;
import com.remitty.caronz.models.HomeCatListModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.helper.OnItemClickListener;
import com.remitty.caronz.home.adapter.ItemHomeAllCategoriesAdapter;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAllCategories extends Fragment {
    RecyclerView categoriesRecycler_view;
    ItemHomeAllCategoriesAdapter itemHomeAllCategories;
    SettingsMain settingsMain;
    RestService restService;
    ArrayList<HomeCatListModel> HomeCatListModels = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();
    private Context context;
    private String method;
    EditText editModel;


    public FragmentAllCategories() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_categories, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        settingsMain = new SettingsMain(context);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            method = bundle.getString("method");

//            Log.e("method: ", method);
        }

        editModel = view.findViewById(R.id.et_model);

        categoriesRecycler_view = view.findViewById(R.id.categoriesRecycler_view);

        categoriesRecycler_view.setHasFixedSize(true);
        categoriesRecycler_view.setNestedScrollingEnabled(false);
        ViewCompat.setNestedScrollingEnabled(categoriesRecycler_view, false);
        GridLayoutManager MyLayoutManager = new GridLayoutManager(context, 3);
        MyLayoutManager.setOrientation(RecyclerView.VERTICAL);
        categoriesRecycler_view.setLayoutManager(MyLayoutManager);
        
        restService = UrlController.createService(RestService.class);

        loadData();

    }

    private void loadData() {
        if (SettingsMain.isConnectingToInternet(getActivity())) {

            if (!HomeActivity.checkLoading)
                SettingsMain.showDilog(getActivity());

            Call<ResponseBody> myCall = restService.getCategories(UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            HomeActivity.checkLoading = false;

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info catLoc obj", "" + response.toString());
                                setAllCategoriesAds(response.getJSONArray("data"));

                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info catLoc ", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info catLoc err", String.valueOf(t));
                        Log.d("info catLoc err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }


    private void setAllCategoriesAds(JSONArray jsonArray) {

        HomeCatListModels.clear();
        categories.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            HomeCatListModel item = new HomeCatListModel(jsonArray.optJSONObject(i));
            HomeCatListModels.add(item);
            categories.add(item.getTitle());
        }

        settingsMain.setCategories(categories);

        itemHomeAllCategories = new ItemHomeAllCategoriesAdapter(context, HomeCatListModels);
        categoriesRecycler_view.setAdapter(itemHomeAllCategories);
        itemHomeAllCategories.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(HomeCatListModel item) {

                FragmentSearch fragment_search = new FragmentSearch();
                Bundle bundle = new Bundle();
                bundle.putString("catId", item.getId());
                bundle.putString("cat_name", item.getTitle());
                bundle.putString("method", method);
                bundle.putString("model", editModel.getText().toString());

                fragment_search.setArguments(bundle);
                replaceFragment(fragment_search, "FragmentSearch");
            }
        });

    }

    void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

}
