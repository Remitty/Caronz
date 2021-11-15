package com.remitty.caronz.profile;

import static com.remitty.caronz.utills.SettingsMain.getMainColor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.phonenumberui.countrycode.Country;
import com.phonenumberui.countrycode.CountryUtils;
import com.remitty.caronz.R;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentUploadActivity extends AppCompatActivity implements RuntimePermissionHelper.permissionInterface{
    SettingsMain settingsMain;
    RestService restService;

    ImageView licenseImage, registrationImage, insuranceImage, otherImage1, otherImage2;
    Button btnSave;

    RuntimePermissionHelper runtimePermissionHelper;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private int choosenImage;
    private String userChoosenTask;

    MultipartBody.Part license, registration, insurance, other1, other2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_upload);

        settingsMain = new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), this);
        runtimePermissionHelper = new RuntimePermissionHelper(this, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getMainColor()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        licenseImage = findViewById(R.id.image_license);
        registrationImage = findViewById(R.id.image_register);
        insuranceImage = findViewById(R.id.image_insurance);
        otherImage1 = findViewById(R.id.image_other1);
        otherImage2 = findViewById(R.id.image_other2);
        btnSave = findViewById(R.id.btnSave);

        licenseImage.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(2));
        registrationImage.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(3));
        insuranceImage.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(4));
        otherImage1.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(5));
        otherImage2.setOnClickListener(view1 -> runtimePermissionHelper.requestStorageCameraPermission(6));
        btnSave.setOnClickListener(view12 -> {
            if (license != null || registration != null || insurance != null || other1 != null) galleryImageUpload();
            else Toast.makeText(getBaseContext(), "No image to upload.", Toast.LENGTH_SHORT).show();
        });


    }

    private void selectImage() {

        final CharSequence[] items;
        items = new CharSequence[]{"camera", "local" };

        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentUploadActivity.this);
        builder.setTitle("Load Image");
        builder.setItems(items, (dialog, item) -> {
            if (item == 0) {
                {
                    userChoosenTask = "Take Photo";
                    cameraIntent();
                }
            } else if (item == 1) {
                {
                    userChoosenTask = "Choose from Library";
                    galleryIntent();
                }
            } else if (item == 3) {
                dialog.dismiss();
            }

        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        Uri tempUri = SettingsMain.getImageUri(this, thumbnail);
        File finalFile = new File(SettingsMain.getRealPathFromURI(this, tempUri));
        galleryPrepare(tempUri);
        switch (choosenImage) {
            case 2:
                licenseImage.setImageURI(tempUri);
                break;
            case 3:
                registrationImage.setImageURI(tempUri);
                break;
            case 4:
                insuranceImage.setImageURI(tempUri);
                break;
            case 5:
                otherImage1.setImageURI(tempUri);
                break;
            case 6:
                otherImage2.setImageURI(tempUri);
                break;
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                Uri tempUri = SettingsMain.getImageUri(this, bm);

                galleryPrepare(tempUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        switch (choosenImage) {
            case 2:
                licenseImage.setImageBitmap(bm);
                break;
            case 3:
                registrationImage.setImageBitmap(bm);
                break;
            case 4:
                insuranceImage.setImageBitmap(bm);
                break;
            case 5:
                otherImage1.setImageBitmap(bm);
                break;
            case 6:
                otherImage2.setImageBitmap(bm);
                break;
        }
    }

    private void galleryPrepare(final Uri absolutePath) {
        final File finalFile = new File(SettingsMain.getRealPathFromURI(this, absolutePath));
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getBaseContext().getContentResolver().getType(absolutePath)),
                        finalFile
                );

        switch (choosenImage) {
            case 2:
                license = MultipartBody.Part.createFormData("license_img", finalFile.getName(), requestFile);
                break;
            case 3:
                registration = MultipartBody.Part.createFormData("registration_img", finalFile.getName(), requestFile);
                break;
            case 4:
                insurance = MultipartBody.Part.createFormData("insurance_img", finalFile.getName(), requestFile);
                break;
            case 5:
                other1 = MultipartBody.Part.createFormData("other1_img", finalFile.getName(), requestFile);
                break;
            case 6:
                other2 = MultipartBody.Part.createFormData("other2_img", finalFile.getName(), requestFile);
                break;
        }
    }

    private void galleryImageUpload() {

        if (SettingsMain.isConnectingToInternet(this)) {

            SettingsMain.showDilog(this);

            Call<ResponseBody> req = restService.postUploadProfileDocument(license, registration, insurance, other1, UrlController.UploadImageAddHeaders(this));
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.v("info Upload Responce", response.toString());
                        if (response.isSuccessful()) {
                            JSONObject responseobj = null;

                            responseobj = new JSONObject(response.body().string());
                            if (responseobj.getBoolean("success")) {
                                try {
                                    Toast.makeText(getBaseContext(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //noinspection ResultOfMethodCallIgnored
//                                finalFile.delete();
                            } else {
                                Toast.makeText(getBaseContext(), responseobj.get("message").toString(), Toast.LENGTH_SHORT).show();
                            }


                        }
                        else {
                            Log.e("update profile error", response.errorBody().string());
                            Toast.makeText(getBaseContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
                        }
                        SettingsMain.hideDilog();
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                    }
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
                        Log.d("info Upload profile", "NullPointert Exception" + t.getLocalizedMessage());
                        SettingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info Upload profile err", String.valueOf(t));
                        Log.d("info Upload profile err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);

        }
    }

    @Override
    public void onSuccessPermission(int code) {
        choosenImage = code;
        selectImage();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        overridePendingTransition(R.anim.left_enter, R.anim.right_out);
    }

   
}