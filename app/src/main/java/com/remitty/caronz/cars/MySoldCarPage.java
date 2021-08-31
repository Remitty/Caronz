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

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MySoldCarPage extends Fragment {
    private ArrayList<CarModel> rentalRealtyList = new ArrayList<>();
    RecyclerView recyclerRental;
    MyCarAdapter mAdapter;

    public MySoldCarPage() {
        // Required empty public constructor
    }

    public static MySoldCarPage newInstance(ArrayList<CarModel> rental) {
        MySoldCarPage fragment = new MySoldCarPage();
        fragment.rentalRealtyList = rental;
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
        View view = inflater.inflate(R.layout.fragment_rental_page, container, false);

        recyclerRental = view.findViewById(R.id.recycler_rental);
        recyclerRental.setLayoutManager(new LinearLayoutManager(getContext()));

        LinearLayout emptyLayout = view.findViewById(R.id.empty_view);

        if(rentalRealtyList.size() > 0) {
            recyclerRental.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerRental.setVisibility(View.GONE);
        }

        mAdapter = new MyCarAdapter(getActivity(), rentalRealtyList);
        recyclerRental.setAdapter(mAdapter);

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
