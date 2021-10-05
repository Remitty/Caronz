package com.remitty.caronz.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.models.HomeCatListModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;

import com.wonshinhyo.dragrecyclerview.DragRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.remitty.caronz.R;
import com.remitty.caronz.helper.LocaleHelper;
import com.remitty.caronz.helper.MyAdsOnclicklinstener;
import com.remitty.caronz.helper.WorkaroundMapFragment;
import com.remitty.caronz.home.adapter.ItemEditImageAdapter;
import com.remitty.caronz.models.myAdsModel;
import com.remitty.caronz.utills.GPSTracker;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;


public class AddNewAdPost extends AppCompatActivity implements OnMapReadyCallback,
         GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, AdapterView.OnItemClickListener,  AdapterView.OnItemSelectedListener,
        RuntimePermissionHelper.permissionInterface {

    private static final String TAG = "Sample";
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    private static final int SELECT_PHOTO = 1000;

    protected GoogleMap mMap;
    AutoCompleteTextView mLocationAutoTextView, mAddressAutoTextView;


    Activity context;
    SettingsMain settingsMain;
    RestService restService;
    RuntimePermissionHelper runtimePermissionHelper;

    FrameLayout frameLayout;
    LinearLayout  linearLayoutMapView;


    LinearLayout page1, page2, page3, linearLayoutImageSection, showHideLocation;

    TextView btnSelectPix, Gallary, tv_done, tvServiceStatement;
    EditText editPostTitle, editPostDescription,
            editTextUserLat, editTextuserLong, editPostRentPrice, editYear, editPostSpeed;

    TextView tvLocation, tvAddress, tvBrand, tvTransmission, tvSpeed, tvSeats, tvName, tvService, tvDescription, tvPrice, tvYear;

    ImageView imageViewBack2, imageViewBack3;
    Button btnNext1, btnNext2, btnPostAd;
    CheckBox featureAdChkBox;
    Spinner catSpinner, transmissionSpinner, seatSpinner, unitsSpinner, currencySpinner, bodySpinner;
    RadioGroup RdgTransmission, rdgService;
    RadioButton rdbAuto, rdbManual, rdbRent, rdbSell, rdbDrive;

    HorizontalScrollView horizontalScrollView;
    ItemEditImageAdapter itemEditImageAdapter;
    List<File> allFile = new ArrayList<>();
    ArrayList<myAdsModel> myImages = new ArrayList<>();;
    DragRecyclerView recyclerView;


    ProgressBar progress_bar;

    FrameLayout loadingLayout;
    private PlacesClient placesClient;
    private NestedScrollView mScrollView;

    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<String> imagePaths = new ArrayList<>();
    private ArrayList<Uri> imagePaths1 = new ArrayList<>();
    private Spinner spinnershow;
    private Calendar myCalendar = Calendar.getInstance();


    EditText placesContainer;
    LinearLayout latlongLayout;
    BottomSheetDialog dialog;
    ListView listView;
    TextView tvAmenties;

    ArrayList<String> places = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();

    LatLng point;
    Boolean packageLimit = false;
    private Boolean spinnerTouched = false;

    int imageRequestCount = 1;
    int totalUploadedImages = 0, currentSize = 0;

    String checkedAmenties="";
    private int currentFileNumber = 1;
    private int totalFiles = 1;

    int imageLimit, per_limit;
    String stringImageLimitText = "";

    String myId;

    ArrayList<String> categories = new ArrayList<String>();
    String[] trans = {"Automatic", "Manual"};
    String[] units = {"Mi", "Km"};

    //    PackagesFragment packagesFragment;
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_new_ad_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(settingsMain.getMainColor()));
        }

        toolbar.setBackgroundColor(Color.parseColor(settingsMain.getMainColor()));

        context = AddNewAdPost.this;
        myId = getIntent().getStringExtra("post_id");

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);
        runtimePermissionHelper = new RuntimePermissionHelper(this, this);
        placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        initComponents();

        initListerners();

        initiImagesAdapter();
        updateViews(settingsMain.getAlertDialogMessage("gmap_lang"));


        categories = settingsMain.getCategories();
        if(categories == null) {
            categories = new ArrayList<String>();
            getCategories();
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            catSpinner.setAdapter(adapter);

        }
        catSpinner.setOnItemSelectedListener(this);


        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, trans);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transmissionSpinner.setAdapter(adapter1);
        transmissionSpinner.setOnItemSelectedListener(this);

//        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, units);
//        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        unitsSpinner.setAdapter(adapter3);
//        unitsSpinner.setOnItemSelectedListener(this);

        String[] seats = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, seats);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seatSpinner.setAdapter(adapter2);
        seatSpinner.setOnItemSelectedListener(this);

        if(myId != null){
            getPostDetail();
        }
    }

    private void initComponents() {
        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);
        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.editorToolbar);

        WorkaroundMapFragment mapFragment = ((WorkaroundMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        mapFragment.setListener(() -> mScrollView.requestDisallowInterceptTouchEvent(true));

        mapFragment.getMapAsync(this);

        frameLayout = (FrameLayout) findViewById(R.id.frame);

        btnSelectPix = (TextView) findViewById(R.id.selectPix);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
//        progress_bar1 = (ProgressBar) findViewById(R.id.progress_bar1);
        Gallary = (TextView) findViewById(R.id.Gallary);
        tv_done = (TextView) findViewById(R.id.tv_done);
        loadingLayout = (FrameLayout) findViewById(R.id.loadingLayout);
        placesContainer = findViewById(R.id.placeContainer);
        latlongLayout = findViewById(R.id.latlongLayout);

        linearLayoutMapView = (LinearLayout) findViewById(R.id.mapViewONOFF);

        editTextUserLat = (EditText) findViewById(R.id.latET);
        editTextuserLong = (EditText) findViewById(R.id.longET);

        page1 = (LinearLayout) findViewById(R.id.line1);
        page2 = (LinearLayout) findViewById(R.id.line2);
        page3 = (LinearLayout) findViewById(R.id.line3);

        page1.setVisibility(View.VISIBLE);
        page2.setVisibility(View.GONE);
        page3.setVisibility(View.GONE);

        recyclerView = (DragRecyclerView) findViewById(R.id.cardView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        GridLayoutManager MyLayoutManager = new GridLayoutManager(context, 3);
        MyLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(MyLayoutManager);

        linearLayoutImageSection = (LinearLayout) findViewById(R.id.ll11);


        editPostTitle = findViewById(R.id.postTitleET);
        editPostTitle.setFocusable(true);
        editPostDescription = findViewById(R.id.postDescriptionET);
        editPostRentPrice = findViewById(R.id.postRentPriceET);
        editPostSpeed = findViewById(R.id.postSpeedET);
        editYear = findViewById(R.id.postYearET);
        tvServiceStatement = findViewById(R.id.tv_service_statement);

        featureAdChkBox = (CheckBox) findViewById(R.id.featureAdChkBox);

        catSpinner = findViewById(R.id.cat_spinner);
        transmissionSpinner = findViewById(R.id.transmission_spinner);
        seatSpinner = findViewById(R.id.seat_spinner);
        unitsSpinner = findViewById(R.id.units_spinner);
        currencySpinner = findViewById(R.id.currency_spinner);
        bodySpinner = findViewById(R.id.body_spinner);


        rdgService = findViewById(R.id.rdg_service);
        rdbRent = findViewById(R.id.rdb_rent);
        rdbSell = findViewById(R.id.rdb_sell);
        rdbDrive = findViewById(R.id.rdb_drive);

//        shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
//                R.anim.shake);

        imageViewBack3 = (ImageView) findViewById(R.id.back3);
        imageViewBack2 = (ImageView) findViewById(R.id.back2);
        btnNext2 = findViewById(R.id.next2);
        btnNext1 = findViewById(R.id.next1);
        btnPostAd = findViewById(R.id.btnPost);

        mLocationAutoTextView = (AutoCompleteTextView) findViewById(R.id
                .location_autoCompleteTextView);
        mAddressAutoTextView = (AutoCompleteTextView) findViewById(R.id
                .address_autoCompleteTextView);

        tvLocation = findViewById(R.id.tv_car_location);
        tvAddress = findViewById(R.id.tv_car_address);
        tvBrand = findViewById(R.id.tv_car_cat);
        tvTransmission = findViewById(R.id.tv_car_transmission);
        tvSpeed = findViewById(R.id.tv_car_speed);
        tvSeats = findViewById(R.id.tv_car_seat);
        tvName = findViewById(R.id.tv_car_name);
        tvYear = findViewById(R.id.tv_car_year);
        tvService = findViewById(R.id.tv_car_service);
        tvDescription = findViewById(R.id.tv_car_description);
        tvPrice = findViewById(R.id.tv_car_price);

    }

    private void initListerners(){
//        placesContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent intent = new PlaceAutocomplete.IntentBuilder()
////                        .accessToken(getString(R.string.access_token))
////                        .placeOptions(PlaceOptions.builder().backgroundColor(Color.parseColor("#EEEEEE")).limit(10).build(PlaceOptions.MODE_CARDS))
////                        .build(AddNewAdPost.this);
////                startActivityForResult(intent, 35);
//
//                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
//
//                // Start the autocomplete intent.
//                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
//                        .setTypeFilter(TypeFilter.CITIES)
//                        .build(getBaseContext());
//                startActivityForResult(intent, 35);
//
//            }
//        });

        btnNext1.setOnClickListener(view -> {
            if (page1Validation()) {
                page1.setVisibility(View.GONE);
                page2.setVisibility(View.VISIBLE);
                page3.setVisibility(View.GONE);
                frameLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.right_enter));
                int selectedId=rdgService.getCheckedRadioButtonId();
                switch(selectedId) {
                    case R.id.rdb_rent:
                        tvServiceStatement.setText("Enter daily rent price.");
                        break;
                    case R.id.rdb_sell:
                        tvServiceStatement.setText("Enter sale price.");
                        break;
                    case R.id.rdb_drive:
                        tvServiceStatement.setText("Enter price per " + getUnit() + ".");
                        break;
                }
            }
        });

        btnNext2.setOnClickListener(view -> {
            if (page2Validation()) {
                page2.setVisibility(View.GONE);
                page3.setVisibility(View.VISIBLE);
                page1.setVisibility(View.GONE);
                page3.setFocusable(true);
                frameLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.right_enter));

                tvLocation.setText(mLocationAutoTextView.getText().toString());
                tvAddress.setText(mAddressAutoTextView.getText().toString());
                tvTransmission.setText(getTransmission());

                int selectedId1=rdgService.getCheckedRadioButtonId();
                switch(selectedId1) {
                    case R.id.rdb_rent:
                        tvService.setText("Rent");
                        tvPrice.setText(getCurrency() + editPostRentPrice.getText().toString() + " /day");
                        break;
                    case R.id.rdb_sell:
                        tvService.setText("Sell");
                        tvPrice.setText(getCurrency() + editPostRentPrice.getText().toString());
                        break;
                    case R.id.rdb_drive:
                        tvService.setText("Drive");
                        tvPrice.setText(getCurrency() + editPostRentPrice.getText().toString() + " /" + getUnit());
                        break;
                }

                tvSpeed.setText(editPostSpeed.getText().toString() + " " + getUnit());
                tvSeats.setText(String.valueOf(seatSpinner.getSelectedItemPosition() + 1));
                tvName.setText(editPostTitle.getText().toString());
                tvYear.setText(editYear.getText().toString());
                tvDescription.setText(editPostDescription.getText().toString());
                tvBrand.setText(settingsMain.getCategory(catSpinner.getSelectedItemPosition()));
            }
        });

        imageViewBack2.setOnClickListener(view -> {
            page2.setVisibility(View.GONE);
            page1.setVisibility(View.VISIBLE);
            page3.setVisibility(View.GONE);
            frameLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.left_enter));
        });

        imageViewBack3.setOnClickListener(view -> {
            page1.setVisibility(View.GONE);
            page2.setVisibility(View.VISIBLE);
            page3.setVisibility(View.GONE);
            frameLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.left_enter));
        });

        btnPostAd.setOnClickListener(view -> {

                submitQuery();

        });

        btnSelectPix.setOnClickListener((View view) -> {
            runtimePermissionHelper.requestStorageCameraPermission(1);
        });

        mAddressAutoTextView.setOnItemClickListener(this);

        mLocationAutoTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                manageAutoComplete(s.toString(), "location");
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mAddressAutoTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                manageAutoComplete(s.toString(), "address");
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private String getTransmission() {
        return trans[transmissionSpinner.getSelectedItemPosition()];
    }

    private String getUnit() {
        return units[unitsSpinner.getSelectedItemPosition()];
    }

    private String getCurrency() {return currencySpinner.getSelectedItem().toString();}

    private String getBody() {return bodySpinner.getSelectedItem().toString();}

    private void getCategories() {
        if (SettingsMain.isConnectingToInternet(this)) {

            if (!HomeActivity.checkLoading)
                SettingsMain.showDilog(AddNewAdPost.this);

            Call<ResponseBody> myCall = restService.getCategories(UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            HomeActivity.checkLoading = false;

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info catLoc obj", "" + response.toString());
                                JSONArray jsonArray = response.getJSONArray("data");
                                categories.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    HomeCatListModel item = new HomeCatListModel(jsonArray.optJSONObject(i));
                                    categories.add(item.getTitle());
                                }

                                settingsMain.setCategories(categories);

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, categories);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                catSpinner.setAdapter(adapter);

                            } else {
                                Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        SettingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getBaseContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
        }
    }

    private void getPostDetail() {
        JsonObject params = new JsonObject();

        params.addProperty("car_id", myId);

        if (SettingsMain.isConnectingToInternet(context)) {

            SettingsMain.showDilog(context);
            Log.d("info adPost Data", "" + params.toString());
            Call<ResponseBody> myCall = restService.getDetail(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        Log.d("info AdPost Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info AdPost object", response.toString());
                                JSONObject data = response.getJSONObject("data");

                                CarModel car = new CarModel(data);

                                JSONArray images = car.getImages();
                                for (int i = 0; i < images.length(); i ++){
                                    updateImages(images.getJSONObject(i));
                                }


                                editPostTitle.setText(car.getName());
                                editYear.setText(car.getYear());
                                editPostDescription.setText(car.getDescription());
                                editPostRentPrice.setText(car.getPrice());
                                mLocationAutoTextView.setText(car.getLocation());
                                if(!car.getAddress().isEmpty())
                                mAddressAutoTextView.setText(car.getAddress());
                                if(!car.getLat().isEmpty())
                                editTextUserLat.setText(car.getLat());
                                if(!car.getLng().isEmpty())
                                editTextuserLong.setText(car.getLng());
                                if(!car.getLat().isEmpty() && !car.getLng().isEmpty()) {
                                    LatLng latLng = new LatLng(Double.parseDouble(car.getLat()), Double.parseDouble(car.getLng()));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                }

                                editPostSpeed.setText(car.getDistance());
                                catSpinner.setSelection(car.getCatId());
                                if(car.getTransmission().equals("Manual"))
                                    transmissionSpinner.setSelection(1);
                                else transmissionSpinner.setSelection(0);
                                if(car.getUnit().equals("Km"))
                                    unitsSpinner.setSelection(1);
                                else unitsSpinner.setSelection(0);
                                seatSpinner.setSelection(Integer.parseInt(car.getSeats()));
                                if(car.isBuy())
                                    rdbSell.setChecked(true);
                                if(car.isRental()) rdbRent.setChecked(true);
                                if(car.isHire()) rdbDrive.setChecked(true);

                            } else {
                                Toast.makeText(context, response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                    Log.d("info AdPost error", String.valueOf(t));
                    Log.d("info AdPost error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(context, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean page2Validation() {
        boolean validate = true;

        if(editPostRentPrice.getText().toString().isEmpty()) {
            editPostRentPrice.setError("!");
            validate = false;
        }

        if(editPostDescription.getText().toString().isEmpty()) {
            editPostDescription.setError("!");
            validate = false;
        }

        if( myImages.size() == 0) {
            Toast.makeText(getBaseContext(), "Please put car images.", Toast.LENGTH_SHORT).show();
            validate = false;
        }

        if(myImages.size() > 4) {
            Toast.makeText(getBaseContext(), "Image limit is 4.", Toast.LENGTH_SHORT).show();
            validate = false;
        }

        return validate;
    }

    private boolean page1Validation() {
        boolean validate = true;

//        if(editPostSpeed.getText().toString().isEmpty()) {
//            editPostSpeed.setError("!");
//            validate = false;
//        }

        if(mLocationAutoTextView.getText().toString().isEmpty()) {
            mLocationAutoTextView.setError("!");
            validate = false;
        }

        if(mAddressAutoTextView.getText().toString().isEmpty()) {
            mAddressAutoTextView.setError("!");
            validate = false;
        }

        if (editPostTitle.getText().toString().isEmpty()) {
            editPostTitle.setError("!");
            validate = false;
        }

        if (editYear.getText().toString().isEmpty()) {
            editYear.setError("!");
            validate = false;
        }

        if (editTextuserLong.getText().toString().isEmpty()) {
            editTextuserLong.setError("!");
            validate = false;
        }

        if (editTextUserLat.getText().toString().isEmpty()) {
            editTextUserLat.setError("!");
            validate = false;
        }

        if(catSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getBaseContext(), "Please select brand.", Toast.LENGTH_SHORT).show();
            validate = false;
        }

        return validate;
    }

    private void manageAutoComplete(String query, String type) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest.Builder request = FindAutocompletePredictionsRequest.builder();
//        request.setCountry("US");
        request.setTypeFilter(TypeFilter.REGIONS);
        request.setSessionToken(token)
                .setQuery(query);

        if(type.equals("address"))
            request.setTypeFilter(TypeFilter.ADDRESS);

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

            ArrayAdapter<?> adapter = new ArrayAdapter<Object>(AddNewAdPost.this, android.R.layout.simple_dropdown_item_1line, data);
            mLocationAutoTextView.setAdapter(adapter);
            mAddressAutoTextView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Places", "Place not found: " + apiException.getStatusCode());
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if(view == mAddressAutoTextView) {
            String placeId = ids.get(position);
            List<com.google.android.libraries.places.api.model.Place.Field> placeFields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG);

// Construct a request object, passing the place ID and fields array.
            FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                    .build();
// Add a listener to handle the response.
            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                com.google.android.libraries.places.api.model.Place place = response.getPlace();
                Log.i("Places", "Place found: " + place.getLatLng().latitude + " " + place.getLatLng().longitude);
                if (mMap != null) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));

                    editTextuserLong.setText(String.format("%s", place.getLatLng().longitude));
                    editTextUserLat.setText(String.format("%s", place.getLatLng().latitude));
                }
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
// Handle error with given status code.
                    Log.e("Places", "Place not found: " + exception.getMessage());
                }
            });

//        }
    }

    public void initiImagesAdapter() {
        imageLimit = 10000;
        per_limit = 4;
        stringImageLimitText = "Too Large";
        myImages = new ArrayList<>();
        itemEditImageAdapter = new ItemEditImageAdapter(context, myImages);
        recyclerView.setAdapter(itemEditImageAdapter);
        itemEditImageAdapter.setHandleDragEnabled(true);
        itemEditImageAdapter.setLongPressDragEnabled(true);
        itemEditImageAdapter.setSwipeEnabled(true);

        itemEditImageAdapter.setOnItemClickListener(new MyAdsOnclicklinstener() {
            @Override
            public void onItemClick(myAdsModel item) {

            }

            @Override
            public void delViewOnClick(View v, int position) {
                myAdsModel item = myImages.get(position);
                if(item.getImageType() == 0) {
                    delImage(item.getAdId(), position, item.getImageType());
                }
//                if(item.getImageType() == 1)
//                    delImage(position+"", item.getImageType());
            }

            @Override
            public void editViewOnClick(View v, int position) {

            }
        });

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void updateViews(String languageCode) {
        LocaleHelper.setLocale(this, languageCode);
    }

    //APi for submiting post to server
    private void submitQuery() {
        JsonObject params = new JsonObject();

        if (itemEditImageAdapter != null) {
            if (itemEditImageAdapter.getItemCount() > 0)
                params.addProperty("images", itemEditImageAdapter.getAllTags());
        } else
            params.addProperty("images", "");
        if(myId != null)
            params.addProperty("car_id", myId);

        params.addProperty("title", editPostTitle.getText().toString());
        params.addProperty("description", editPostDescription.getText().toString());
        params.addProperty("price", editPostRentPrice.getText().toString());
        params.addProperty("currency", getCurrency());
        params.addProperty("body", getBody());
        params.addProperty("unit", getUnit());
        params.addProperty("seat", (seatSpinner.getSelectedItemPosition() + 1));
        params.addProperty("transmission", getTransmission());
        params.addProperty("distance", editPostSpeed.getText().toString());
        params.addProperty("cat_id", catSpinner.getSelectedItemPosition());
        params.addProperty("location", mLocationAutoTextView.getText().toString());
        params.addProperty("address", mAddressAutoTextView.getText().toString());
        params.addProperty("year", editYear.getText().toString());
//            try {
//                String phoneNumber = editTextUserPhone.getText().toString();
//                if (jsonObj.getBoolean("is_phone_verification_on")) {
//                    if (phoneNumber.contains("+"))
//                        params.addProperty(jsonObj.getJSONObject("phone").getString("field_type_name"), phoneNumber);
//                    else
//                        params.addProperty(jsonObj.getJSONObject("phone").getString("field_type_name"), "+" + phoneNumber);
//                } else
//                    params.addProperty(jsonObj.getJSONObject("phone").getString("field_type_name"), phoneNumber);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        if (!mLocationAutoTextView.getText().toString().equals("")) {
//            params.addProperty("location", mLocationAutoTextView.getText().toString());
//        } else {
//            params.addProperty("address", placesContainer.getText().toString());
//        }

        params.addProperty("location_lat", editTextUserLat.getText().toString());
        params.addProperty("location_lng", editTextuserLong.getText().toString());
//            if (jsonObj.getBoolean("ad_country_show")) {
//                subcatDiloglist subDiloglist = (subcatDiloglist) spinnerLocation.getSelectedView().getTag();
//                params.addProperty("ad_country", subDiloglist.getId());
//            }
//        if (featureAdChkBox.isChecked()) {
//            params.addProperty("isFeatured", "true");
//        } else
//            params.addProperty("isFeatured", "false");

        int selectedId1=rdgService.getCheckedRadioButtonId();
        switch(selectedId1) {
            case R.id.rdb_rent:
                params.addProperty("service", "rent");
                break;
            case R.id.rdb_sell:
                params.addProperty("service", "buy");
                break;
            case R.id.rdb_drive:
                params.addProperty("service", "hire");
                break;
        }

        if (SettingsMain.isConnectingToInternet(context)) {

            SettingsMain.showDilog(context);
            Log.d("info adPost Data", "" + params.toString());
            Call<ResponseBody> myCall = restService.postAdNewPost(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                            Log.d("info AdPost Resp", "" + responseObj.toString());
                        if (responseObj.isSuccessful()) {

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info AdPost object", response.toString());

                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                alert.setIcon(R.mipmap.ic_launcher)
                                        .setTitle("Congratulation!!!")
                                        .setMessage(response.getString("message"))
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(AddNewAdPost.this, HomeActivity.class);
                                                startActivity(intent);
                                                AddNewAdPost.this.finish();
                                            }
                                        }).show();


                            } else {
                                alertMsg(response.getString("message"));
                            }
                        } else {
                            Log.e("post car error: ", responseObj.errorBody().string());
                            alertMsg(responseObj.errorBody().string());
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
                    Log.d("info AdPost error", String.valueOf(t));
                    Log.d("info AdPost error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(context, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    private void alertMsg(String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(R.mipmap.ic_launcher)
                .setTitle("Alert")
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    // getting images selected from gallery for post and sending them to server
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                imagePaths = new ArrayList<>();
                imageRequestCount = 1;
                imagePaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                if (SettingsMain.isConnectingToInternet(context)) {
                    if (imagePaths.size() > 0) {
                        btnSelectPix.setEnabled(false);
                        AsyncImageTask asyncImageTask = new AsyncImageTask();
                        asyncImageTask.execute(imagePaths);
                    }
                }
            } else {
                btnSelectPix.setEnabled(true);
                Toast.makeText(context, settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 35) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                placesContainer.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }

//        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
//
//            Uri uri = data.getData();
//            Bitmap bitmap = null;
//
////          imagePaths1 = new ArrayList<>();
//            imageRequestCount = 1;
//            imagePaths1.add(uri);
//            List<MultipartBody.Part> parts = null;
//            parts = new ArrayList<>();
//            parts.add(prepareFilePart("file" + 0, uri));
//
//            uploadImages(parts);
//
//            addImage(uri);
//
//        }

    }

    private void addImage(Uri uri) {
        myAdsModel item = new myAdsModel();
        String lastTag="0";
        if(myImages.size() > 0) {
            myAdsModel last = myImages.get(myImages.size() - 1);
            lastTag = last.getAdId();
        }

        item.setAdId((Integer.parseInt(lastTag)+1)+"");
        item.setImageType(1);
        item.setImage(uri);

        myImages.add(item);

        itemEditImageAdapter.notifyDataSetChanged();
    }

    public void goToImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);
    }

    private void uploadImages(MultipartBody.Part parts) {
        Log.d("info car id", myId + "");
        if(myId == null) myId = "";

        RequestBody adID =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, myId);

        final Call<ResponseBody> req = restService.postUploadImage(adID, parts);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    Log.v("info UploadImage", response.toString());
                try {
                if (response.isSuccessful()) {

                    JSONObject responseobj = null;

                        responseobj = new JSONObject(response.body().string());
                        Log.d("info UploadImage object", "" + responseobj.getJSONObject("data").toString());
                        if (responseobj.getBoolean("success")) {

                            updateImages(responseobj.getJSONObject("data"));

                            int selectedImageSize = imagePaths.size();
                            int totalSize = currentSize + selectedImageSize;
                            Log.d("info image2", "muImage" + totalSize + "imagePaths" + totalUploadedImages);
                                UploadSuccessImage();
                            if (totalSize == totalUploadedImages) {
                                imagePaths.clear();
                                paths.clear();
                                if (allFile.size() > 0) {
                                    for (File file : allFile) {
                                        if (file.exists()) {
                                            if (file.delete()) {
                                                Log.d("file Deleted :", file.getPath());
                                            } else {
                                                Log.d("file not Deleted :", file.getPath());
                                            }
                                        }
                                    }
                                }
                            }

                        } else {
                            UploadFailedImage();
                            Toast.makeText(context, responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                        }



                    btnSelectPix.setEnabled(true);

                } else {
                    UploadFailedImage();
                    Toast.makeText(getBaseContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
                }
                } catch (JSONException e) {
                    UploadFailedImage();
                    e.printStackTrace();
                } catch (IOException e) {
                    UploadFailedImage();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("info Upload Image Err:", t.toString());

                if (t instanceof TimeoutException) {
                    UploadFailedImage();

//                    requestForImages();
                }
                if (t instanceof SocketTimeoutException) {
                    UploadFailedImage();
//                    requestForImages();
//
                } else {
                    UploadFailedImage();
//                    requestForImages();
                }
            }
        });

    }

    private void UploadFailedImage() {
        progress_bar.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        btnSelectPix.setEnabled(true);
//        Toast.makeText(context, progressModel.getFailMessage(), Toast.LENGTH_SHORT).show();

    }

    private void UploadSuccessImage() {
        progress_bar.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
//        Toast.makeText(context, progressModel.getSuccessMessage(), Toast.LENGTH_SHORT).show();
        btnSelectPix.setEnabled(true);
    }

    private MultipartBody.Part prepareFilePart(String fileName, Uri fileUri) {

        File finalFile = new File(SettingsMain.getRealPathFromURI(context, fileUri));
        allFile.add(finalFile);
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        finalFile
                );
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(fileName, finalFile.getName(), requestFile);
    }

    private File rotateImage(String path) {
        File file = null;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        file = new File(getRealPathFromURI(getImageUri(rotatedBitmap)));
        allFile.add(file);
        return file;
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    //setting spiner error for validation
    private void setSpinnerError(Spinner spinner) {
        View selectedView = spinner.getSelectedView();
        if (selectedView != null && selectedView instanceof TextView) {
            spinner.requestFocus();
            TextView selectedTextView = (TextView) selectedView;
            selectedTextView.setError("Required!");
            selectedTextView.setTextColor(Color.RED);
        }
    }

    @Override
    public void onMapReady(GoogleMap Map) {
        mMap = Map;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            return;
        }
        GPSTracker gpsTracker = new GPSTracker(AddNewAdPost.this);
        if (!gpsTracker.canGetLocation())
            gpsTracker.showSettingsAlert();
        else if (gpsTracker.canGetLocation() && gpsTracker.isCheckPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        LatLng currentPos = null;
        currentPos = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        // create marker
        MarkerOptions marker = new MarkerOptions()
                .position(currentPos)
//                .title(gpsTracker.get)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        mMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude())).zoom(16).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                2000, null);


        try {
            mAddressAutoTextView.setText(gpsTracker.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        editTextUserLat.setText(String.format("%s", gpsTracker.getLatitude()));
        editTextuserLong.setText(String.format("%s", gpsTracker.getLongitude()));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            editTextuserLong.setText(String.format("%s", latLng.longitude));
            editTextUserLat.setText(String.format("%s", latLng.latitude));
//            List<Address> addresses2 = new ArrayList<>();
//            try {
//                addresses2 = new Geocoder(this, Locale.getDefault()).getFromLocation(latLng.longitude, latLng.latitude, 1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            StringBuilder result1 = new StringBuilder();
//            if (addresses2.size() > 0) {
//                Address address = addresses2.get(0);
//                int maxIndex = address.getMaxAddressLineIndex();
//                for (int x = 0; x <= maxIndex; x++) {
//                    result1.append(address.getAddressLine(x));
//                    //result.append(",");
//                }
//            }
//            try {
//                mAddressAutoTextView.setText(result1.toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        });
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
//        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        GPSTracker gpsTracker = new GPSTracker(AddNewAdPost.this);
        if (!gpsTracker.canGetLocation())
            gpsTracker.showSettingsAlert();
        else {
            Geocoder geocoder;
            List<Address> addresses1 = null;
            try {
                addresses1 = new Geocoder(this, Locale.getDefault()).getFromLocation(gpsTracker.getLatitude(), gpsTracker.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder result = new StringBuilder();
            if (addresses1.size() > 0) {
                Address address = addresses1.get(0);
                int maxIndex = address.getMaxAddressLineIndex();
                for (int x = 0; x <= maxIndex; x++) {
                    result.append(address.getAddressLine(x));
                    //result.append(",");
                }
            }
            try {
                mAddressAutoTextView.setText(result.toString());
                editTextuserLong.setText(gpsTracker.getLongitude() + "");
                editTextUserLat.setText(gpsTracker.getLatitude() + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //start fragment
//    public void startFragment(Fragment someFragment, String tag) {
//        FragmentManager fm = getSupportFragmentManager();
//        Fragment fragment = fm.findFragmentByTag(tag);
//        if (fragment == null) {
//            fragment = someFragment;
//            fm.beginTransaction()
//                    .add(R.id.frame, fragment, tag).commit();
//        }
//    }
    


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(myId == null)
            allRemoveImages();
        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }

    private void allRemoveImages() {
        myAdsModel item = null;

        if(myImages.size() == 0) return;

        for(int i = 0; i < myImages.size(); i ++){
            item = myImages.get(i);

            delImage(item.getAdId()+"",i, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                break;
        }
        return true;
    }

    private void delImage(String tag, int position, int imageType) {

        if(imageType == 1){
            myImages.remove(Integer.parseInt(tag));
            itemEditImageAdapter.notifyDataSetChanged();
            return;
        }

        if (SettingsMain.isConnectingToInternet(context)) {
            loadingLayout.setVisibility(View.VISIBLE);
//            SettingsMain.showDilog(context);
            JsonObject params = new JsonObject();
            params.addProperty("img_id", tag);
            params.addProperty("car_id", myId);
            Log.d("info send DeleteImage", params.toString());
            Call<ResponseBody> myCall = restService.postDeleteImages(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info DeleteImage Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                Log.d("info DeleteImage object", "" + response.toString());
                                myImages.remove(position);
                                itemEditImageAdapter.notifyDataSetChanged();
                                Gallary.setVisibility(View.VISIBLE);
                                Gallary.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle_black_24dp, 0, 0, 0);
                                tv_done.setVisibility(View.GONE);

//                                Toast.makeText(context, response.get("message").toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, response.get("message").toString(), Toast.LENGTH_SHORT).show();
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
                    loadingLayout.setVisibility(View.GONE);
                    Log.d("info DeleteImage error", t.getStackTrace().toString());
                    Log.d("info DeleteImage error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                }
            });

        } else {
            SettingsMain.hideDilog();
            Toast.makeText(context, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    void initiImageList(JSONObject object) {
        myAdsModel item = new myAdsModel();

//        object = jsonArrayImages.getJSONObject(i);
        String image_path = "";
        try {
            totalUploadedImages ++;

//            JSONObject object = jsonObject.getJSONObject("ad_images");
            item.setAdId(object.getString("id"));

            if (object.optString("thumb").startsWith("http"))
                image_path = object.optString("thumb");
            else
                image_path = UrlController.ASSET_ADDRESS + object.optString("thumb");
            item.setImage(image_path);
            item.setImageType(0);
            myImages.add(item);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        myImages.clear();
//        try {
//
//
//            JSONArray jsonArrayImages = jsonObject.getJSONArray("ad_images");
//
//            totalUploadedImages = jsonArrayImages.length();
//            Log.d("info Images Data", "" + jsonArrayImages.toString());
//
//            if (jsonArrayImages.length() > 0) {
//
//                for (int i = 0; i < jsonArrayImages.length(); i++) {
//                    myAdsModel item = new myAdsModel();
//                    JSONObject object = new JSONObject();
//                    object = jsonArrayImages.getJSONObject(i);
//
//                    item.setAdId(object.getString("img_id"));
//                    item.setImage((object.getString("thumb")));
//                    item.setImageType(0);
//                    myImages.add(item);
//                }
//
//            } else {
////                recyclerView.setAdapter(null);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    void updateImages(JSONObject jsonObject) {
        initiImageList(jsonObject);
        itemEditImageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showDate(final EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewAdPost.this, (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            if (editText != null)
                editText.setText(sdf.format(myCalendar.getTime()));
        }, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private List<String> getSelectedItems() {
        List<String> result = new ArrayList<>();
        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
        checkedAmenties="";
        if(checkedItems.size() > 0)
        for (int i = 0; i < checkedItems.size(); ++i) {
            if (checkedItems.valueAt(i)) {
                result.add((String) listView.getItemAtPosition(checkedItems.keyAt(i)));
                checkedAmenties += (checkedItems.keyAt(i)+1)+",";
            }
        }

        return result;
    }

    @Override
    public void onSuccessPermission(int code) {
//        if (imageLimit < per_limit) {
//            per_limit = imageLimit;
//        }
//        if (imageLimit > 0) {
            FilePickerBuilder.getInstance().setMaxCount(per_limit)
                    .setSelectedFiles(paths)
                    .setActivityTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar)
                    .pickPhoto(AddNewAdPost.this);
//        goToImageIntent();
//            Toast.makeText(context, stringImageLimitText, Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, stringImageLimitText, Toast.LENGTH_SHORT).show();
//        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class AsyncImageTask extends AsyncTask<ArrayList<String>, Void, List<MultipartBody.Part>> {
        ArrayList<String> imaagesLis = null;
        boolean checkDimensions = true, checkImageSize;

        @SafeVarargs
        @Override
        protected final List<MultipartBody.Part> doInBackground(ArrayList<String>... params) {
            List<MultipartBody.Part> parts = null;
            imaagesLis = params[0];
            totalFiles = imaagesLis.size();
            for (int i = 0; i < imaagesLis.size(); i++) {
                parts = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                Log.d("info image", currentDateandTime);
                checkDimensions = true;
                checkImageSize = true;
//                if (adPostImageModel.getDim_is_show()) {
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = true;
//                    BitmapFactory.decodeFile(imaagesLis.get(i), options);
//                    int imageWidth = options.outWidth;
//                    int imageHeight = options.outHeight;
//                    if (imageHeight > Integer.parseInt(adPostImageModel.getDim_height()) &&
//                            imageWidth > Integer.parseInt(adPostImageModel.getDim_width())) {
//                        checkDimensions = true;
//                        Log.d("treuwala", String.valueOf(checkDimensions));
//                    } else {
//                        checkDimensions = false;
//
//                        Log.d("falsewala", String.valueOf(checkDimensions));
//                        runOnUiThread(() -> Toast.makeText(context, adPostImageModel.getDim_height_message(), Toast.LENGTH_SHORT).show());
//                    }
//                }
                checkImageSize = true;
                checkDimensions = true;
                File file = new File(imaagesLis.get(i));
//                long fileSizeInBytes = file.length();
//                Integer fileSizeBytes = Math.round(fileSizeInBytes);
//                if (fileSizeBytes > Integer.parseInt(adPostImageModel.getImg_size())) {
//                    checkImageSize = false;
//                    Log.d("falsewalasize", String.valueOf(checkImageSize));
//                    runOnUiThread(() -> Toast.makeText(context, adPostImageModel.getImg_message(), Toast.LENGTH_SHORT).show());
//                } else {
//                    checkImageSize = true;
//                    Log.d("truewalasize", String.valueOf(checkImageSize));
//                }
                if (checkImageSize && checkDimensions) {
                    File finalFile1 = rotateImage(imaagesLis.get(i));
//                    File finalFile1 =new File(imaagesLis.get(i));

                    Uri tempUri = SettingsMain.decodeFile(context, finalFile1);

//                    parts.add(prepareFilePart("file" + i, tempUri));

//                    uploadImages(parts);
                    uploadImages(prepareFilePart("image", tempUri));
                }
            }
            return parts;
        }

        @Override
        protected void onPostExecute(List<MultipartBody.Part> result) {
        }

        @Override
        protected void onPreExecute() {
            currentSize = myImages.size();
            progress_bar.setVisibility(View.VISIBLE);

            Drawable drawable = getResources().getDrawable(R.drawable.bg_uploading).mutate();
            drawable.setColorFilter(Color.parseColor(SettingsMain.getMainColor()), PorterDuff.Mode.SRC_ATOP);
            loadingLayout.setBackground(drawable);

            loadingLayout.setVisibility(View.VISIBLE);
        }

    }
}
