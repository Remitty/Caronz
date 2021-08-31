package com.remitty.caronz.cars.adapters;

import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.cars.MySoldCarPage;
import com.remitty.caronz.cars.MyActiveCarPage;
import com.remitty.caronz.cars.MyRentalCarPage;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MyCarPageAdapter extends FragmentStatePagerAdapter {

    private ArrayList<CarModel> myRealtyList = new ArrayList<>();
    private ArrayList<CarModel> rentalRealtyList = new ArrayList<>();
    private ArrayList<CarModel> rentalHistoryList = new ArrayList<>();

    public MyCarPageAdapter(FragmentManager fm, ArrayList<CarModel> list, ArrayList<CarModel> rental, ArrayList<CarModel> rentalHistory) {
        super(fm);
        this.myRealtyList = list;
        this.rentalRealtyList = rental;
        this.rentalHistoryList = rentalHistory;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MyActiveCarPage.newInstance(this.myRealtyList);
            case 1:
                return MyRentalCarPage.newInstance(this.rentalRealtyList);
            case 2:
                return MySoldCarPage.newInstance(this.rentalHistoryList);
            default:

                return null;
        }
    }


    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Active";
            case 1:
                return "Rental";
            case 2:
                return "Sold";
        }
        return null;
    }

    @Override
    public int getItemPosition(Object object){
        if(object instanceof MyActiveCarPage){
            return POSITION_NONE;
        }
        if(object instanceof MyRentalCarPage){
            return POSITION_NONE;
        }
        if(object instanceof MySoldCarPage){
            return POSITION_NONE;
        }
        return 1;
    }

}
