package com.remitty.caronz.utills;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.remitty.caronz.models.UserModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.remitty.caronz.R;
import com.remitty.caronz.home.helper.AdPostImageModel;
import com.remitty.caronz.home.helper.CalanderTextModel;
import com.remitty.caronz.home.helper.Location_popupModel;
import com.remitty.caronz.home.helper.ProgressModel;
import com.remitty.caronz.models.permissionsModel;
import com.remitty.caronz.utills.NoInternet.NetwordStateManager;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsMain {
    public static final String PREF_NAME = "com.adforest";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 2, NETWORK_STAUS_WIFI = 1, NETWORK_STATUS_MOBILE = 0;
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 0;
    public static int TYPE_NOT_CONNECTED = 2;
    private static Dialog dialog1;
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    public String languagecode = "";
    public String locationId = "";

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public SettingsMain(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /* Checking Internet Connection */
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {

                        return true;
                    }
        }
        return false;
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
                activeNetwork = cm.getActiveNetworkInfo();
                Log.d("info d ", activeNetwork.getType() + "" + activeNetwork.getTypeName());
            }
        }


        if (null != activeNetwork) {
            Log.d("info adssad", "adasd");
            if (activeNetwork.getType() == TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        Log.d("info d", getConnectivityStatus(context) + "");
        int conn = getConnectivityStatus(context);
        int status = 0;
        if (conn == TYPE_WIFI) {
            status = NETWORK_STAUS_WIFI;
        } else if (conn == TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }

    public static void enableInternetReceiver(Context context) {
        ComponentName component = new ComponentName(context, NetwordStateManager.class);

        context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void disableInternetReceiver(Context context) {
        Log.d("info check net", "disable net");
        ComponentName component = new ComponentName(context, NetwordStateManager.class);
        context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

    }

    public static boolean isInternetReceiverEnabled(Context context) {
        ComponentName component = new ComponentName(context, NetwordStateManager.class);
        int status = context.getPackageManager().getComponentEnabledSetting(component);
        if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return true;
        } else if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            return false;
        }
        return false;

    }

    public static void showDilog(Context context) {
        SettingsMain settingsMain = new SettingsMain(context);

        dialog1 = new Dialog(context, R.style.AppTheme);
//        dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog1.setContentView(R.layout.dilog_progressbar);
        dialog1.setCancelable(false);
        TextView textView = dialog1.findViewById(R.id.id_title);
        textView.setText(settingsMain.getAlertDialogMessage("waitMessage"));
        dialog1.show();
    }

    public static void hideDilog() {
        dialog1.dismiss();
    }

    public static void showAlertDialog(final Context context, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(context.getResources().getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), decoded, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Context inContext, Uri uri) {
        @SuppressLint("Recycle") Cursor cursor = inContext.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                return cursor.getString(idx);

            }
        } catch (Exception e) {
            Log.d("info GetReal Path Error", e.toString());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch (Exception ex) {
                Log.d("info GetReal Path Error", ex.toString());

            }
        }

        return "";
    }

    public static void reload(Context context, String tag) {
        Fragment frg;
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();

        frg = manager.findFragmentByTag(tag);
        final FragmentTransaction ft = manager.beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    public static boolean isSocial(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String restoredText = pref.getString("isSocial", "false");
        return restoredText.equals("true");
    }

    public static Uri decodeFile(Context context, File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//
//       File destFile = new File(file, "img_"
//                + dateFormatter.format(new Date()).toString() + ".png");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), b, "Title", null);
        return Uri.parse(path);
    }

    public static void adforest_changeRattingBarcolor(RatingBar ratingBar, Context context) {
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(context.getResources().getColor(R.color.gradientfifth), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(context.getResources().getColor(R.color.rattingBarColor), PorterDuff.Mode.SRC_ATOP);

    }



    public static String getLanguageCode() {
        return pref.getString("languageCode", "en");
    }

    public void setLanguageCode(String languageCode) {
        editor.putString("languageCode", languageCode);
        editor.commit();
    }
    public void setLocationId(String locationId){
        editor.putString("locationId",locationId);
        editor.commit();
    }
public static String getlocationId(){ return pref.getString("locationId", "");}
    public static permissionsModel getPermissionsModel() {


        Gson gson = new Gson();
        String permissionsModel = pref.getString("permissionsModel", null);
        permissionsModel model = gson.fromJson(permissionsModel, permissionsModel.class);
        return model;
    }

    public static void setPermissionsModel(permissionsModel permissionsModel) {
        Gson gson = new Gson();
        String toJson = gson.toJson(permissionsModel);
        editor.putString("permissionsModel", toJson);
        editor.apply();

    }

    public static void setProgressModel(ProgressModel progressModel) {
        Gson gson = new Gson();
        String toJson = gson.toJson(progressModel);
        editor.putString("progressModel", toJson);
        editor.apply();

    }

    public static ProgressModel getProgressSettings(Context context) {


        Gson gson = new Gson();
        String progressModel = pref.getString("progressModel", null);
        ProgressModel model = gson.fromJson(progressModel, ProgressModel.class);
        return model;
    }

    public static String getMainColor() {
        return pref.getString("mainColor", "#199603");
    }

    public void setMainColor(String mainColor) {
        editor.putString("mainColor", mainColor);
        editor.commit();
    }

    public String getAlertDialogMessage(String type) {
        return pref.getString(type, "Loading...");
    }

    public void setAlertDialogMessage(String type, String msg) {
        editor.putString(type, msg);
        editor.commit();
    }

    public String getKey(String name) {
        return pref.getString(name, "");
    }

    public void setKey(String name, String value) {
        editor.putString(name, value);
        editor.commit();
    }

    public String getAlertDialogTitle(String type) {
        return pref.getString(type, "Error");
    }

    public void setAlertDialogTitle(String type, String title) {
        editor.putString(type, title);
        editor.commit();
    }

    public String getAlertOkText() {
        return pref.getString("AlertOkText", "OK");
    }

    public void setAlertOkText(String msg) {
        editor.putString("AlertOkText", msg);
        editor.commit();
    }

    public String getAlertCancelText() {
        return pref.getString("AlertCancelText", "CANCEL");
    }

    public void setAlertCancelText(String msg) {
        editor.putString("AlertCancelText", msg);
        editor.commit();
    }public String getPaidMessage() {
        return pref.getString("setPaidMessage", "CANCEL");
    }

    public void setPaidMessage(String msg) {
        editor.putString("setPaidMessage", msg);
        editor.commit();
    }

    public void setUser(String userLogin) {
        editor.putString("user", userLogin);
        editor.commit();
    }

    public String getUser() {
        return pref.getString("user", "");
    }

    public Integer getUserId() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getId();
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getUserImage() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getPicture();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getUserName() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getFirstName() + " " + user.getLastName();
        } catch (JSONException e) {
            e.printStackTrace();
            return "User Name";
        }
    }

    public String getUserFirstName() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getFirstName();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getUserSecondName() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getLastName();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getUserEmail() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getEmail();
        } catch (JSONException e) {
            e.printStackTrace();
            return "Email";
        }
    }

    public String getUserPhone() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getPhone();
        } catch (JSONException e) {
            e.printStackTrace();
            return "Email";
        }
    }

    public String getUserAddress() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getFirstAddress() + ", " + user.getSecondAddress();
        } catch (JSONException e) {
            e.printStackTrace();
            return "Email";
        }
    }

    public String getUserLocation() {
        UserModel user = null;
        try {
            user = new UserModel(new JSONObject(getUser()));
            return user.getCity() + ", " + user.getState() + ", " + user.getCountry();
        } catch (JSONException e) {
            e.printStackTrace();
            return "Email";
        }
    }

    public void setAuthToken(String UserPassword) {
        editor.putString("access_token", UserPassword);
        editor.commit();
    }

    public String getAuthToken() {
        return pref.getString("access_token", "");
    }

    public String getUserPassword() {
        return pref.getString("userPassword", "0");
    }

    public void setUserPassword(String UserPassword) {
        editor.putString("userPassword", UserPassword);
        editor.commit();
    }
    public String getFireBaseId() {
        return pref.getString("firebaseid", "");
    }

    public void setFireBaseId(String value) {
        editor.putString("firebaseid", value);
        editor.commit();
    }

    public boolean getRTL() {
        return pref.getBoolean("RTL", false);
    }

    public String getGenericAlertTitle() {
        return pref.getString("title", "Confirm!");
    }


    public String getGenericAlertMessage() {
        return pref.getString("text", "Are You Sure You Want To Do This!");
    }

    public String getGenericAlertOkText() {
        return pref.getString("btn_ok", "OK");
    }

    public void setGenericAlertOkText(String title) {
        editor.putString("btn_ok", title);
        editor.commit();
    }

    public String getGenericAlertCancelText() {
        return pref.getString("btn_no", "Cancel");
    }

    public void setGenericAlertCancelText(String title) {
        editor.putString("btn_no", title);
        editor.commit();
    }

    public void setAppOpen(boolean appOpen) {
        editor.putBoolean("app_open", appOpen);
        editor.commit();
    }

    public boolean getAppOpen() {
        return pref.getBoolean("app_open", false);// for test: false
    }

    public void setProfileComplete(boolean appOpen) {
        editor.putBoolean("profile_complete", appOpen);
        editor.commit();
    }

    public boolean getProfileComplete() {
        return pref.getBoolean("profile_complete", false);// for test: false
    }

    public void checkOpen(boolean appOpen) {
        editor.putBoolean("checkOpen", appOpen);
        editor.commit();
    }

    public String getGuestImage() {
        return pref.getString("guest_image", "");
    }

    public void setGuestImage(String message) {
        editor.putString("guest_image", message);
        editor.commit();
    }

    public boolean getCheckOpen() {
        return pref.getBoolean("checkOpen", false);
    }

    public String getNoLoginMessage() {
        return pref.getString("noLoginmessage", "Please login to perform this action.");
    }

    public void setNoLoginMessage(String message) {
        editor.putString("noLoginmessage", message);
        editor.commit();
    }

    public boolean isFeaturedScrollEnable() {
        return pref.getBoolean("featured_scroll_enabled", false);
    }

    public void setFeaturedScrollEnable(boolean featuredScrollEnable) {
        editor.putBoolean("featured_scroll_enabled", featuredScrollEnable);
        editor.commit();
    }

    public int getFeaturedScroolDuration() {
        return pref.getInt("featured_duration", 40);

    }

    public void setFeaturedScroolDuration(int duration) {
        editor.putInt("featured_duration", duration);
        editor.commit();
    }

    public int getFeaturedScroolLoop() {
        return pref.getInt("featured_loop", 40);

    }

    public void setFeaturedScroolLoop(int duration) {
        editor.putInt("featured_loop", duration);
        editor.commit();
    }


    //region LocationPopup

    public void setPaymentCompletedMessage(String paymentCompletedMessage) {
        editor.putString("message", paymentCompletedMessage);
        editor.commit();
    }

    public String getpaymentCompletedMessage() {
        return pref.getString("message", "Order Places Succ");
    }

    public String getGpsTitle() {
        return pref.getString("gpsTitle", "GPS AppCompatPreferenceActivity");
    }

    public void setGpsTitle(String gpsTitle) {
        editor.putString("gpsTitle", gpsTitle);
        editor.commit();
    }

    public String getGpsText() {
        return pref.getString("gpsText", "GPS is not enabled. Do you want to go to settings menu?");
    }

    public void setGpsText(String gpsText) {
        editor.putString("gpsText", gpsText);
        editor.commit();
    }

    public String getGpsConfirm() {
        return pref.getString("gpsConfirm", "AppCompatPreferenceActivity");
    }

    public void setGpsConfirm(String gpsConfirm) {
        editor.putString("gpsConfirm", gpsConfirm);
        editor.commit();
    }

    public String getGpsCancel() {
        return pref.getString("gpsCancel", "Clear");
    }

    public void setGpsCancel(String gpsCancel) {
        editor.putString("gpsCancel", gpsCancel);
        editor.commit();
    }

    public void setShowNearby(boolean b) {
        editor.putBoolean("show_nearby", b);
        editor.commit();
    }

    public boolean getShowHome() {
        return pref.getBoolean("show_home", false);
    }
    public void setShowHome(boolean b) {
        editor.putBoolean("show_home", b);
        editor.commit();
    }public boolean getShowAdvancedSearch() {
        return pref.getBoolean("show_advanced", false);
    }
    public void setShowAdvancedSearch(boolean b) {
        editor.putBoolean("show_advanced", b);
        editor.commit();
    }

    public boolean getShowNearBy() {
        return pref.getBoolean("show_nearby", false);
    }

    public boolean getAdsPositionSorter() {
        return pref.getBoolean("ads_position_sorter", false);
    }

    public void setAdsPositionSorter(boolean b) {
        editor.putBoolean("ads_position_sorter", b);
        editor.commit();
    }

    public String getLatitude() {
        return pref.getString("nearby_latitude", "");
    }

    public void setLatitude(String latitude) {
        editor.putString("nearby_latitude", latitude);
        editor.commit();
    }

    //endregion

    public String getLongitude() {
        return pref.getString("nearby_longitude", "");
    }

    public void setLongitude(String longitude) {
        editor.putString("nearby_longitude", longitude);
        editor.commit();
    }

    public String getDistance() {
        return pref.getString("nearby_distance", "");
    }

    public void setDistance(String longitude) {
        editor.putString("nearby_distance", longitude);
        editor.commit();
    }

    public String getNotificationTitle() {
        return pref.getString("notificationTitle", "");
    }

    public void setNotificationTitle(String notificationTitle) {
        editor.putString("notificationTitle", notificationTitle);
        editor.commit();
    }

    public String getNotificationMessage() {
        return pref.getString("notificationMessage", "");
    }

    public void setNotificationMessage(String notificationMessage) {
        editor.putString("notificationMessage", notificationMessage);
        editor.commit();
    }

    public String getNotificationImage() {
        return pref.getString("notificationImage", "");
    }

    public void setNotificationImage(String notificationImage) {
        editor.putString("notificationImage", notificationImage);
        editor.commit();
    }

    public boolean getUserVerified() {
        return pref.getBoolean("UserVerified", true);
    }

    public void setUserVerified(boolean userVerified) {
        editor.putBoolean("UserVerified", userVerified);
        editor.commit();
    }

    public  boolean isLocationChanged(){return  pref.getBoolean("isLocationChanged",false);}

    public void  setLocationChanged(boolean b){
        editor.putBoolean("isLocationChanged", b);
        editor.commit();
    }


    public void setLocationPopupModel(Location_popupModel locationPopupModel) {
        Gson gson = new Gson();
        String toJson = gson.toJson(locationPopupModel);
        editor.putString("locationPopupModel", toJson);
        editor.apply();

    }

    public Location_popupModel getLocationPopupModel(Context context) {
        Gson gson = new Gson();
        String progressModel = pref.getString("locationPopupModel", null);
        Location_popupModel model = gson.fromJson(progressModel, Location_popupModel.class);
        return model;
    }

    public void setAdPostImageModel(AdPostImageModel AdPostImageModel) {
        Gson gson = new Gson();
        String toJson = gson.toJson(AdPostImageModel);
        editor.putString("AdPostImageModel", toJson);
        editor.apply();

    }

    public AdPostImageModel getAdPostImageModel(Context context) {
        Gson gson = new Gson();
        String progressModel = pref.getString("AdPostImageModel", null);
        AdPostImageModel model = gson.fromJson(progressModel, AdPostImageModel.class);
        return model;
    }

    public void setCalanderTextModel(CalanderTextModel calanderTextModel) {
        Gson gson = new Gson();
        String toJson = gson.toJson(calanderTextModel);
        editor.putString("calanderTextModel", toJson);
        editor.apply();

    }

    public CalanderTextModel getCalanderTextModel(Context context) {
        Gson gson = new Gson();
        String progressModel = pref.getString("calanderTextModel", null);
        CalanderTextModel model = gson.fromJson(progressModel, CalanderTextModel.class);
        return model;
    }

    public void setCategories(ArrayList<String> categories) {
        Type type = new TypeToken<List<String>>() {
        }.getType();

        String strBecons = new Gson().toJson(categories, type);
        pref.edit().putString("categories", strBecons).apply();
    }

    public ArrayList<String> getCategories() {
        Type listOfBecons = new TypeToken<List<String>>() {
        }.getType();

        ArrayList<String> categories = new Gson().fromJson(pref.getString("categories", ""), listOfBecons);
        return categories;
    }

    public String getCategory(int index) {
        Type listOfBecons = new TypeToken<List<String>>() {
        }.getType();

        ArrayList<String> categories = new Gson().fromJson(pref.getString("categories", ""), listOfBecons);
        return categories.get(index);
    }

    public static void savePopupSettings(AdForest_PopupModel model, Context context) {
        Gson gson = new Gson();
        String popupSetings = gson.toJson(model);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("popup_settings", popupSetings);
        editor.apply();

    }

    public void setTax(String tax) {
        editor.putString("app_tax", tax);
        editor.commit();
    }

    public static String getTax() {
        return pref.getString("app_tax", "");
    }

    public void setCommission(String tax) {
        editor.putString("app_commission", tax);
        editor.commit();
    }

    public static String getCommission() {
        return pref.getString("app_commission", "");
    }

    public static AdForest_PopupModel getPopupSettings(Context context) {


        Gson gson = new Gson();
        String popupSettings = PreferenceManager.getDefaultSharedPreferences(context).getString("popup_settings", null);
        AdForest_PopupModel model = gson.fromJson(popupSettings, AdForest_PopupModel.class);
        return model;
    }

    public void setDeviceToken(String token) {
        editor.putString("device_token", token);
        editor.commit();
    }

    public static String getDeviceToken() {
        return pref.getString("device_token", "");
    }

}