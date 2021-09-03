package com.remitty.caronz.cars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.remitty.caronz.R;
import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.cars.adapters.MyCarAdapter;
import com.remitty.caronz.home.AddNewAdPost;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.RuntimePermissionHelper;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import java.util.ArrayList;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyActiveCarPage extends Fragment implements RuntimePermissionHelper.permissionInterface{

    private ArrayList<CarModel> carList = new ArrayList<>();

    SettingsMain settingsMain;
    RestService restService;

    RecyclerView MyRecyclerView, recyclerViewFeatured;

    MyCarAdapter mAdapter;

    LinearLayout viewHomesLayout;
    NestedScrollView scrollView;
    private RuntimePermissionHelper runtimePermissionHelper;
    private int selected;

    public MyActiveCarPage() {
        // Required empty public constructor
    }

    public static MyActiveCarPage newInstance(ArrayList<CarModel> list) {
        MyActiveCarPage fragment = new MyActiveCarPage();
        fragment.carList = list;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_realty_page, container, false);
        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class);
        runtimePermissionHelper = new RuntimePermissionHelper(getActivity(), this);

        viewHomesLayout = view.findViewById(R.id.filter_layout);

        scrollView = view.findViewById(R.id.scrollView);

        MyRecyclerView = view.findViewById(R.id.recycler_view);
        MyRecyclerView.setHasFixedSize(true);
        MyRecyclerView.setNestedScrollingEnabled(false);
        MyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new MyCarAdapter(getActivity(), carList);
        MyRecyclerView.setAdapter(mAdapter);

        LinearLayout emptyLayout = view.findViewById(R.id.empty_view);

        if(carList.size() == 0) {

            emptyLayout.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }
        else {
            scrollView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }

        if(carList.size() > 0)
            viewHomesLayout.setVisibility(View.VISIBLE);

        mAdapter.setOnItemClickListener(new MyCarAdapter.Listener() {
            @Override
            public void onItemClick(int position) {
                CarModel item = carList.get(position);
                Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                intent.putExtra("carId", item.getId());
                startActivity(intent);
            }

            @Override
            public void onEdit(int position) {
                selected = position;
                runtimePermissionHelper.requestStorageCameraPermission(1);

            }

            @Override
            public void onDelete(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to delete this car?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestDelete();
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

            @Override
            public void onActive(int position) {

            }
        });

        return view;
    }

    private void requestDelete() {
    }

    @Override
    public void onSuccessPermission(int code) {
        CarModel item = carList.get(selected);
        Intent intent = new Intent(getActivity(), AddNewAdPost.class);
        intent.putExtra("post_id", item.getId());
        startActivity(intent);
    }
}
