package com.remitty.caronz.cars.adapters;


import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.models.HireModel;
import com.remitty.caronz.utills.SettingsMain;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyHiredAdapter extends RecyclerView.Adapter<MyHiredAdapter.CustomViewHolder> {

    SettingsMain settingsMain;
    private List<HireModel> realtyList = new ArrayList<>();
    private Context mContext;
    private MyHiredAdapter.Listener ItemClickListener;
    private boolean show = false;

    public MyHiredAdapter(Context context, List<HireModel> realtyList) {
        this.realtyList = realtyList;
        this.mContext = context;
        settingsMain = new SettingsMain(context);

    }

    public MyHiredAdapter(Context context, List<HireModel> realtyList, boolean flag) {
        this.realtyList = realtyList;
        this.mContext = context;
        this.show = flag;
        settingsMain = new SettingsMain(context);

    }

    @Override
    public MyHiredAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_hired, viewGroup, false);
        return new MyHiredAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHiredAdapter.CustomViewHolder customViewHolder, int i) {
        final HireModel feedItem = realtyList.get(i);
        CarModel car = feedItem.getCar();

        customViewHolder.tvAddress.setText(feedItem.getProcessDate());
        customViewHolder.tvPrice.setText(car.getCurrency() + feedItem.getSubTotal());
        customViewHolder.tvTitle.setText(car.getCatName() + " " + car.getName());
        customViewHolder.tvStatus.setText(feedItem.getBookStatus());
        customViewHolder.tvTerm.setText(feedItem.getBookFrom());
        customViewHolder.tvPickup.setText(feedItem.getPickupLocation());
        customViewHolder.tvDropoff.setText(feedItem.getDropoffLocation());
        customViewHolder.tvEstDistance.setText(feedItem.getEstDistance());
        customViewHolder.tvEstTime.setText(feedItem.getEstTime());

        if(!car.getFirstImage().isEmpty())
            Picasso.with(mContext).load(car.getFirstImage())
                .resize(270, 270).centerCrop()
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(customViewHolder.imageView);


        if(feedItem.getBookStatus().equals("Hired"))
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

        if(feedItem.getBookStatus().equals("Hired") || feedItem.getBookStatus().equals("Confirmed"))
            customViewHolder.imgDelete.setVisibility(View.GONE);


        customViewHolder.btnBookConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemConfirm(i);
            }
        });

        customViewHolder.btnBookCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemCancel(i);
            }
        });

        customViewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemRemove(i);
            }
        });

        customViewHolder.copyDropOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText(customViewHolder.tvDropoff.getText().toString());
            }
        });

        customViewHolder.copyPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText(customViewHolder.tvPickup.getText().toString());
            }
        });

    }
    private void copyText(String str) {
        ClipboardManager manager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", str);
        manager.setPrimaryClip(clipData);
        Toast.makeText(mContext, "Copied successfully", Toast.LENGTH_SHORT).show();
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

    public MyHiredAdapter.Listener getOnItemClickListener() {
        return ItemClickListener;
    }

    public void setOnItemClickListener(MyHiredAdapter.Listener onItemClickListener) {
        this.ItemClickListener = onItemClickListener;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private final RatingBar ratingBar;
        ImageView imageView, copyPickup, copyDropOff, imgDelete;
        TextView tvTitle, tvAddress, tvTerm, tvPrice, tvStatus, tvPickup, tvDropoff, tvEstTime, tvEstDistance;
        Button btnBookCancel, btnBookConfirm;
        LinearLayout btnGroupLayout;

        CustomViewHolder(View view) {
            super(view);

            this.imageView = view.findViewById(R.id.realty_image);
            this.tvTitle = view.findViewById(R.id.realty_title);
            this.tvAddress = view.findViewById(R.id.realty_date);
            this.tvTerm = view.findViewById(R.id.realty_term);
            this.tvPrice = view.findViewById(R.id.realty_price);
            this.tvStatus = view.findViewById(R.id.realty_status);
            this.btnBookCancel = view.findViewById(R.id.btn_book_cancel);
            this.btnBookConfirm = view.findViewById(R.id.btn_book_confirm);
            this.btnGroupLayout = view.findViewById(R.id.btn_group_layout);
            this.tvPickup = view.findViewById(R.id.pickup_location);
            this.tvDropoff = view.findViewById(R.id.dropoff_location);
            this.tvEstTime = view.findViewById(R.id.est_time);
            this.tvEstDistance = view.findViewById(R.id.est_distance);
            this.ratingBar = view.findViewById(R.id.ratingBar);
            this.copyPickup = view.findViewById(R.id.copy_pickup);
            this.copyDropOff = view.findViewById(R.id.copy_dropoff);
            this.imgDelete = view.findViewById(R.id.img_delete);
        }
    }

    public interface Listener {
        /**
         * @param position
         */
        void onItemCancel(int position);
        void onItemConfirm(int position);
        void onItemRemove(int position);
    }
}
