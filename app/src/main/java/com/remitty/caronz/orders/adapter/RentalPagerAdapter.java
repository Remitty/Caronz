package com.remitty.caronz.orders.adapter;

import com.remitty.caronz.orders.BookingHistory;
import com.remitty.caronz.orders.Upcoming;
import com.remitty.caronz.models.RentalModel;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class RentalPagerAdapter extends FragmentPagerAdapter {

    private List<RentalModel> bookingUpcomingRealtyList = new ArrayList<>();
    private List<RentalModel> bookingHistoryRealtyList = new ArrayList<>();

    public RentalPagerAdapter(FragmentManager fm, List<RentalModel> upcoming, List<RentalModel> history) {
        super(fm);
        this.bookingUpcomingRealtyList = upcoming;
        this.bookingHistoryRealtyList = history;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return Upcoming.newInstance(this.bookingUpcomingRealtyList);
            case 1:
                return BookingHistory.newInstance(this.bookingHistoryRealtyList);
            default:

                return null;
        }
    }


    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Upcoming";
            case 1:
                return "Booking History";
        }
        return null;
    }
}
