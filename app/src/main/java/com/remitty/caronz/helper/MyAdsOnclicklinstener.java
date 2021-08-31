package com.remitty.caronz.helper;

import android.view.View;

import com.remitty.caronz.models.myAdsModel;

public interface MyAdsOnclicklinstener {

    void onItemClick(myAdsModel item);

    void delViewOnClick(View v, int position);

    void editViewOnClick(View v, int position);

}
