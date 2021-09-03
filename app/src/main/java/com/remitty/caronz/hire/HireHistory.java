package com.remitty.caronz.hire;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.remitty.caronz.R;
import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.hire.adapter.HireAdapter;
import com.remitty.caronz.models.HireModel;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HireHistory extends Fragment {
    RecyclerView recyclerHistory;
    HireAdapter mAdapter;
    private List<HireModel> bookingRealtyList = new ArrayList<>();

    public HireHistory() {
        // Required empty public constructor
    }

    public static HireHistory newInstance(List<HireModel> history) {
        HireHistory fragment = new HireHistory();
        fragment.bookingRealtyList = history;
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
        View view = inflater.inflate(R.layout.fragment_hire_history, container, false);


        recyclerHistory = view.findViewById(R.id.recycler_history);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        LinearLayout emptyLayout = view.findViewById(R.id.empty_view);

        if(bookingRealtyList.size() > 0) {
            recyclerHistory.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerHistory.setVisibility(View.GONE);
        }

        mAdapter = new HireAdapter(getActivity(), bookingRealtyList, 0);
        recyclerHistory.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new HireAdapter.Listener(){
            @Override
            public void onItemEdit(int position) {
            }

            @Override
            public void onItemCancel(int position) {
            }

            @Override
            public void onItemClick(int position) {
                HireModel item = bookingRealtyList.get(position);
                Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                intent.putExtra("carId", item.getCarId());
                startActivity(intent);
            }

            @Override
            public void onItemConfirm(int position) {

            }

        });
        mAdapter.notifyDataSetChanged();
        return view;
    }
}
