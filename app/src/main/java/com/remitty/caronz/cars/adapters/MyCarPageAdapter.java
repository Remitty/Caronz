package com.remitty.caronz.cars.adapters;

import com.remitty.caronz.cars.MyHiredCarPage;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.cars.MySoldCarPage;
import com.remitty.caronz.cars.MyActiveCarPage;
import com.remitty.caronz.cars.MyRentalCarPage;
import com.remitty.caronz.models.HireModel;
import com.remitty.caronz.models.OrderModel;
import com.remitty.caronz.models.RentalModel;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MyCarPageAdapter extends FragmentStatePagerAdapter {

    private ArrayList<CarModel> myActiveCarList = new ArrayList<>();
    private ArrayList<RentalModel> myRentalCarList = new ArrayList<>();
    private ArrayList<HireModel> myHireCarList = new ArrayList<>();
    private ArrayList<OrderModel> mySoldCarList = new ArrayList<>();

    public MyCarPageAdapter(FragmentManager fm, ArrayList<CarModel> myActiveCarList, ArrayList<RentalModel> myRentalCarList, ArrayList<HireModel> myHireCarList, ArrayList<OrderModel> mySoldCarList) {
        super(fm);
        this.myActiveCarList = myActiveCarList;
        this.myRentalCarList = myRentalCarList;
        this.myHireCarList = myHireCarList;
        this.mySoldCarList = mySoldCarList;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MyActiveCarPage.newInstance(this.myActiveCarList);
            case 1:
                return MyRentalCarPage.newInstance(this.myRentalCarList);
            case 2:
                return MyHiredCarPage.newInstance(this.myHireCarList);
            case 3:
                return MySoldCarPage.newInstance(this.mySoldCarList);
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
                return "Hired";
            case 3:
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
        if(object instanceof MyHiredCarPage){
            return POSITION_NONE;
        }
        if(object instanceof MySoldCarPage){
            return POSITION_NONE;
        }
        return 1;
    }

}
