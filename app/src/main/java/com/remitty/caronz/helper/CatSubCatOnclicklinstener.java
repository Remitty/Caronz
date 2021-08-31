package com.remitty.caronz.helper;

import android.view.View;

import com.remitty.caronz.models.RentalModel;

public interface CatSubCatOnclicklinstener {
    void onItemClick(RentalModel item);

    void onItemTouch(RentalModel item);

    void addToFavClick(View v, String position);

}
