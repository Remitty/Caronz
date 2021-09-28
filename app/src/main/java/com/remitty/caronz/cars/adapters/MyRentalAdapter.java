package com.remitty.caronz.cars.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.RentalModel;
import com.remitty.caronz.utills.SettingsMain;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyRentalAdapter extends RecyclerView.Adapter<MyRentalAdapter.CustomViewHolder> {

    SettingsMain settingsMain;
    private List<RentalModel> realtyList = new ArrayList<>();
    private Context mContext;
    private MyRentalAdapter.Listener ItemClickListener;
    private boolean show = false;

    public MyRentalAdapter(Context context, List<RentalModel> realtyList) {
        this.realtyList = realtyList;
        this.mContext = context;
        settingsMain = new SettingsMain(context);

    }

    public MyRentalAdapter(Context context, List<RentalModel> realtyList, boolean flag) {
        this.realtyList = realtyList;
        this.mContext = context;
        this.show = flag;
        settingsMain = new SettingsMain(context);

    }

    @Override
    public MyRentalAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_rental_car, viewGroup, false);
        return new MyRentalAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyRentalAdapter.CustomViewHolder customViewHolder, int i) {
        final RentalModel feedItem = realtyList.get(i);

        customViewHolder.tvAddress.setText(feedItem.getProcessdate());
        customViewHolder.tvPrice.setText(feedItem.getCar().getCurrency() + feedItem.getSubTotal());
        customViewHolder.tvTitle.setText(feedItem.getCar().getCatName() + " " + feedItem.getCar().getName());
        customViewHolder.tvStatus.setText(feedItem.getBookStatus());
        customViewHolder.tvStart.setText(feedItem.getBookFrom());
        customViewHolder.tvEnd.setText(feedItem.getBookTo());
        Picasso.with(mContext).load(feedItem.getCar().getFirstImage())
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(customViewHolder.imageView);

        if(feedItem.getBookStatus().equals("Booked"))
            customViewHolder.btnGroupLayout.setVisibility(View.VISIBLE);
        else
            customViewHolder.btnGroupLayout.setVisibility(View.GONE);

        if(!feedItem.getBookStatus().equals("Completed")) {
            customViewHolder.ratingBar.setVisibility(View.GONE);
        } else {
            customViewHolder.ratingBar.setRating(feedItem.getRate());
            LayerDrawable stars = (LayerDrawable) customViewHolder.ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);
        }


        customViewHolder.btnBookConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemConfirm(i);
            }
        });

       
        customViewHolder.btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemCancel(i);
            }
        });



    }
    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }
    private final static int FADE_DURATION = 1000; //FADE_DURATION in milliseconds

    @Override
    public int getItemCount() {
        return (null != realtyList ? realtyList.size() : 0);
    }

    public MyRentalAdapter.Listener getOnItemClickListener() {
        return ItemClickListener;
    }

    public void setOnItemClickListener(MyRentalAdapter.Listener onItemClickListener) {
        this.ItemClickListener = onItemClickListener;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvTitle, tvAddress, tvStart, tvEnd, tvPrice, tvStatus;
        Button btnDecline, btnBookConfirm;
        LinearLayout btnGroupLayout;
        RatingBar ratingBar;

        CustomViewHolder(View view) {
            super(view);

            this.imageView = view.findViewById(R.id.realty_image);
            this.tvTitle = view.findViewById(R.id.realty_title);
            this.tvAddress = view.findViewById(R.id.realty_date);
            this.tvStart = view.findViewById(R.id.rental_start);
            this.tvEnd = view.findViewById(R.id.rental_end);
            this.tvPrice = view.findViewById(R.id.realty_price);
            this.tvStatus = view.findViewById(R.id.realty_status);
            this.btnDecline = view.findViewById(R.id.btn_book_cancel);
            this.btnBookConfirm = view.findViewById(R.id.btn_book_confirm);
            this.btnGroupLayout = view.findViewById(R.id.btn_group_layout);
            this.ratingBar = view.findViewById(R.id.ratingBar);
        }
    }

    public interface Listener {
        /**
         * @param position
         */
        void onItemCancel(int position);
        void onItemConfirm(int position);
    }
}