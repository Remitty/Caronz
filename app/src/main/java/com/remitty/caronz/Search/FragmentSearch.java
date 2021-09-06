package com.remitty.caronz.Search;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.remitty.caronz.R;
import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.adapters.CarAdapter;
import com.remitty.caronz.helper.EndlessNestedScrollViewListener;
import com.remitty.caronz.home.FragmentAllCategories;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.JsonObject;

public class FragmentSearch extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, MenuItem.OnMenuItemClickListener, AdapterView.OnItemClickListener{

    private String catId="0", method;
    private int pageNumber = 0;
    RestService restService;
    LinearLayout emptyLayout;
    static boolean title_Nav;
    private SettingsMain settingsMain;
    private RecyclerView mRecyclerView;
    private NestedScrollView scrollView;
    private Context context;

    public static View locationFragmentView;
    public static AlertDialog locationDialog;
    boolean back_pressed = false;
    private View mView;
    private CarAdapter carAdapter;

    ImageButton btnCat;

    AutoCompleteTextView currentLocationText;
    RuntimePermissionHelper runtimePermissionHelper;
    private PlacesClient placesClient;
    ArrayList<String> places = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();

    private ArrayList<CarModel> carsList = new ArrayList<>();
    private boolean isMore = false;
    private String location, model;

    public FragmentSearch() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    public static void setData(Boolean title_nav) {
        FragmentSearch.title_Nav = title_nav;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();
        this.getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                //This is the filter
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        backPressed();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_search, container, false);
        context = getActivity();
        settingsMain = new SettingsMain(getActivity());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            catId = bundle.getString("catId");
            if(catId == null) catId = "0";
            method = bundle.getString("method");
            model = bundle.getString("model");
        }

        restService = UrlController.createService(RestService.class);
        placesClient = com.google.android.libraries.places.api.Places.createClient(context);

        scrollView = mView.findViewById(R.id.scrollView);
        carAdapter = new CarAdapter(getActivity(), carsList, method);
        carAdapter.setOnItemClickListener(new CarAdapter.Listener() {
            @Override
            public void onItemClick(int position) {
                CarModel car = carsList.get(position);
                Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                intent.putExtra("carId", car.getId());
                intent.putExtra("method", method);
                startActivity(intent);
            }

        });
        mRecyclerView = mView.findViewById(R.id.recycler_view);
//        mRecyclerView.setHasFixedSize(true);
        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);
        mRecyclerView.setAdapter(carAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        ViewCompat.setNestedScrollingEnabled(mRecyclerView, true);

        ((HomeActivity) getActivity()).changeImage();

        getActivity().setTitle(getResources().getString(R.string.app_name));

        btnCat = mView.findViewById(R.id.btn_cat);
        btnCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentAllCategories fragment_cat = new FragmentAllCategories();
                Bundle bundle = new Bundle();
                bundle.putString("method", method);
                fragment_cat.setArguments(bundle);
                replaceFragment(fragment_cat, "FragmentAllCategories");
            }
        });

        emptyLayout = mView.findViewById(R.id.empty_view);

        currentLocationText = mView.findViewById(R.id.et_location);
        currentLocationText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                manageAutoComplete(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

        });
        currentLocationText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    location = currentLocationText.getText().toString();
                    submitQuery();
                }
                return false;
            }
        });



        initNestedScrollView();

        submitQuery();

        return mView;

    }

    private void manageAutoComplete(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest.Builder request = FindAutocompletePredictionsRequest.builder();

//        request.setCountry("US");
        request.setTypeFilter(TypeFilter.REGIONS);
        request.setSessionToken(token)
                .setQuery(query);


        placesClient.findAutocompletePredictions(request.build()).addOnSuccessListener((response) -> {

            ids.clear();
            places.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                places.add(prediction.getFullText(null).toString());
                ids.add(prediction.getPlaceId());
                Log.i("Places", prediction.getPlaceId());
                Log.i("Places", prediction.getFullText(null).toString());
            }
            String[] data = places.toArray(new String[places.size()]); // terms is a List<String>

            ArrayAdapter<?> adapter = new ArrayAdapter<Object>(getActivity(), android.R.layout.simple_dropdown_item_1line, data);
            currentLocationText.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Places", "Place not found: " + apiException.getStatusCode());
            }
        });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem action_location = menu.findItem(R.id.action_location);
        action_location.setVisible(false);
        action_location.setOnMenuItemClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null) {
                Uri s = data.getData();

//                tv_location.setText(s.toString());

            }
        }
    }

    private void backPressed() {
        if (!back_pressed) {
            Toast.makeText(getContext(), "Press Again To Exit", Toast.LENGTH_SHORT).show();
            back_pressed = true;
            android.os.Handler mHandler = new android.os.Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    back_pressed = false;
                }
            }, 2000L);
        } else {
            ViewDialog viewDialog = new ViewDialog();
            viewDialog.showDialog(getActivity(), "Are you sure you want to exit?");
//            this.finishAffinity();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
//                runtimePermissionHelper.requestLocationPermission(2);
//                locationSearch();
                break;
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    //category not paid dialog
    public class ViewDialog {

        public void showDialog(Activity activity, String msg) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.popup_delete);
//            ImageView imageView = dialog.findViewById(R.id.a);
//            Glide.with(getContext()).load(R.drawable.angry).into(imageView);
            TextView text = (TextView) dialog.findViewById(R.id.txt_delete_job);
            text.setText(msg);

            Button dialogButton = (Button) dialog.findViewById(R.id.btn_confirm);
            dialogButton.setBackgroundColor(Color.parseColor(SettingsMain.getMainColor()));
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finishAffinity();
                    dialog.dismiss();
                }
            });
            Button dialogButtonCancel = (Button) dialog.findViewById(R.id.btn_close);
            dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                }
            });

            dialog.show();

        }
    }

    private void initNestedScrollView() {
        if (scrollView != null) {
            scrollView.setOnScrollChangeListener(new EndlessNestedScrollViewListener(mRecyclerView.getLayoutManager()){
                @Override
                public void onLoadMore(int page) {
                    isMore = true;
                    showMoreLoading();
                    pageNumber = page+1;
                    Log.d("page number", page+"");
                    submitQuery();
                }
            });
        }
    }

    private void submitQuery() {

        if (SettingsMain.isConnectingToInternet(getActivity())) {
            if(!isMore) {
                emptyLayout.setVisibility(View.VISIBLE);
                if (!HomeActivity.checkLoading)
                    SettingsMain.showDilog(getActivity());
            }
            JsonObject object = new JsonObject();
            object.addProperty("cat_id", catId);
            object.addProperty("model", model);
            object.addProperty("service", method);
            if(location != null)
            object.addProperty("location", location);
            object.addProperty("page_number", pageNumber);
            Log.e("search param: ", object.toString());
            Call<ResponseBody> myCall = restService.getSearchDetails(object, UrlController.AddHeaders(getActivity()));
            myCall.enqueue(new Callback<ResponseBody>() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {

                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());

                            if (response.getBoolean("success")) {
                                JSONArray data = response.getJSONArray("data");
                                Log.d("home data", data.toString());
                                carsList.clear();
                                for (int i = 0; i < data.length(); i ++) {
                                    CarModel car = new CarModel(data.getJSONObject(i));
                                    carsList.add(car);
                                }
                                carAdapter.loadMore(carsList);
                                HomeActivity.checkLoading = false;

                                if(data.length() > 0) {
                                    scrollView.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                }
                                else {
                                    emptyLayout.setVisibility(View.VISIBLE);
                                    scrollView.setVisibility(View.GONE);
                                }

                            } else {
                                Toast.makeText(getActivity(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), responseObj.errorBody().string(), Toast.LENGTH_SHORT).show();
                        }
                        SettingsMain.hideDilog();

                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SettingsMain.hideDilog();
                    Log.d("info HomeGet error", String.valueOf(t));
//                    Timber.d(String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(getActivity(), "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    private void showMoreLoading() {
        ProgressBar progressBar = mView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideMoreLoading() {
        isMore = false;
        ProgressBar progressBar = mView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

}
