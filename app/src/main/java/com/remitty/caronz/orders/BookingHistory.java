package com.remitty.caronz.orders;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.remitty.caronz.R;
import com.remitty.caronz.car_detail.CarDetailActivity;
import com.remitty.caronz.models.RentalModel;
import com.remitty.caronz.orders.adapter.RentalAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BookingHistory extends Fragment {
    RecyclerView recyclerHistory;
    RentalAdapter mAdapter;
    private List<RentalModel> bookingRealtyList = new ArrayList<>();

    public BookingHistory() {
        // Required empty public constructor
    }

    public static BookingHistory newInstance(List<RentalModel> history) {
        BookingHistory fragment = new BookingHistory();
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
        View view = inflater.inflate(R.layout.fragment_booking_history, container, false);


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

        mAdapter = new RentalAdapter(getActivity(), bookingRealtyList);
        recyclerHistory.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new RentalAdapter.Listener(){
            @Override
            public void onItemEdit(int position) {
            }

            @Override
            public void onItemCancel(int position) {
            }

            @Override
            public void onItemClick(int position) {
                RentalModel item = bookingRealtyList.get(position);
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
