package com.remitty.caronz.cars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.remitty.caronz.R;
import com.remitty.caronz.cars.adapters.MyCarAdapter;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.utills.Network.RestService;
import com.remitty.caronz.utills.SettingsMain;
import com.remitty.caronz.utills.UrlController;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyRentalCarPage extends Fragment {
    private ArrayList<CarModel> rentalCarList = new ArrayList<>();
    RecyclerView recyclerUpcoming;
    MyCarAdapter mAdapter;
    LinearLayout emptyLayout;
    private boolean type = false;

    SettingsMain settingsMain;
    RestService restService;

    public MyRentalCarPage() {
        // Required empty public constructor
    }

    public static MyRentalCarPage newInstance(ArrayList<CarModel> upcoming) {
        MyRentalCarPage fragment = new MyRentalCarPage();
        fragment.rentalCarList = upcoming;
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
        View view = inflater.inflate(R.layout.fragment_upcoming_realty_page, container, false);

        recyclerUpcoming = view.findViewById(R.id.recycler_upcoming);
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new MyCarAdapter(getActivity(), rentalCarList);
        recyclerUpcoming.setAdapter(mAdapter);

        settingsMain = new SettingsMain(getActivity());
        restService = UrlController.createService(RestService.class, settingsMain.getAuthToken(), getActivity());

        emptyLayout = view.findViewById(R.id.empty_view);

        if(rentalCarList.size() > 0) {
            recyclerUpcoming.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerUpcoming.setVisibility(View.GONE);
        }

        mAdapter.setOnItemClickListener(new MyCarAdapter.Listener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onEdit(int position) {

            }

            @Override
            public void onDelete(int position) {

            }

            @Override
            public void onActive(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getContext().getResources().getString(R.string.app_name))
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Are you sure you want to activate this car again?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestActivate();
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

        mAdapter.notifyDataSetChanged();

        return view;
    }

    private void requestActivate() {
    }

}
