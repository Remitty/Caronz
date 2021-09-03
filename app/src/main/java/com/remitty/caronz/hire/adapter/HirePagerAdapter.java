package com.remitty.caronz.hire.adapter;

import com.remitty.caronz.hire.HireHistory;
import com.remitty.caronz.hire.HireUpcoming;
import com.remitty.caronz.orders.BookingHistory;
import com.remitty.caronz.models.HireModel;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class HirePagerAdapter extends FragmentPagerAdapter {

    private List<HireModel> bookingUpcomingRealtyList = new ArrayList<>();
    private List<HireModel> bookingHistoryRealtyList = new ArrayList<>();

    public HirePagerAdapter(FragmentManager fm, List<HireModel> upcoming, List<HireModel> history) {
        super(fm);
        this.bookingUpcomingRealtyList = upcoming;
        this.bookingHistoryRealtyList = history;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return HireUpcoming.newInstance(this.bookingUpcomingRealtyList);
            case 1:
                return HireHistory.newInstance(this.bookingHistoryRealtyList);
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
                return "Hire History";
        }
        return null;
    }
}
