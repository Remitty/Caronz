package com.remitty.caronz.orders.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

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

import com.remitty.caronz.models.RentalModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.remitty.caronz.R;
import com.remitty.caronz.utills.SettingsMain;

public class RentalAdapter extends RecyclerView.Adapter<RentalAdapter.CustomViewHolder> {

    SettingsMain settingsMain;
    private List<RentalModel> realtyList = new ArrayList<>();
    private Context mContext;
    private Listener ItemClickListener;
    private boolean show = false;

    public RentalAdapter(Context context, List<RentalModel> realtyList) {
        this.realtyList = realtyList;
        this.mContext = context;
        settingsMain = new SettingsMain(context);

    }

    public RentalAdapter(Context context, List<RentalModel> realtyList, boolean flag) {
        this.realtyList = realtyList;
        this.mContext = context;
        this.show = flag;
        settingsMain = new SettingsMain(context);

    }

    @Override
    public RentalAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_booking_realty, viewGroup, false);
        return new RentalAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RentalAdapter.CustomViewHolder customViewHolder, int i) {
        final RentalModel feedItem = realtyList.get(i);

        customViewHolder.tvDate.setText(feedItem.getProcessdate());
        customViewHolder.tvPrice.setText(feedItem.getCurrency() + feedItem.getBookTotal());
        if(feedItem.getPayment().equals("Cash") || feedItem.getPayment().equals("Balance")  || feedItem.getPayment().equals("Card")) {
            customViewHolder.tvTitle.setText(feedItem.getCar().getName());
            customViewHolder.avisLayout.setVisibility(View.GONE);
            Picasso.with(mContext).load(feedItem.getCar().getFirstImage())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }
        else {
            customViewHolder.tvTitle.setText(feedItem.getCarId());
            customViewHolder.tvDropoffLocation.setText(feedItem.getDropoffLocation());
            customViewHolder.tvPickupLocation.setText(feedItem.getPickupLocation());
            customViewHolder.tvConfirmationNo.setText(feedItem.getConfirmationNumber());
            if(feedItem.getCarImage() != null)
                Picasso.with(mContext).load(feedItem.getCarImage())
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(customViewHolder.imageView);
        }

        customViewHolder.tvStatus.setText(feedItem.getBookStatus());
        customViewHolder.tvStart.setText(feedItem.getBookFrom());
        customViewHolder.tvEnd.setText(feedItem.getBookTo());
        customViewHolder.tvSource.setText(feedItem.getPayment());

        if(show) {
            if(feedItem.getBookStatus().equals("Booked"))
                customViewHolder.btnBookConfirm.setVisibility(View.GONE);
        } else {
            customViewHolder.btnGroupLayout.setVisibility(View.GONE);
        }

        if(!feedItem.getBookStatus().equals("Completed")) {
            customViewHolder.ratingBar.setVisibility(View.GONE);
        } else {
            customViewHolder.ratingBar.setRating(feedItem.getRate());
            LayerDrawable stars = (LayerDrawable) customViewHolder.ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);
        }



        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemClick(i);
            }
        };

        customViewHolder.btnBookConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemConfirm(i);
            }
        });

        customViewHolder.btnBookEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemEdit(i);
            }
        });
        customViewHolder.btnBookCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemCancel(i);
            }
        });

        customViewHolder.itemView.setOnClickListener(listener);



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

    public Listener getOnItemClickListener() {
        return ItemClickListener;
    }

    public void setOnItemClickListener(Listener onItemClickListener) {
        this.ItemClickListener = onItemClickListener;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private final RatingBar ratingBar;
        ImageView imageView;
        TextView tvTitle, tvDate, tvStart, tvEnd, tvPrice, tvStatus, tvSource;
        Button btnBookEdit, btnBookCancel, btnBookConfirm;
        LinearLayout btnGroupLayout, avisLayout;
        TextView tvPickupLocation, tvDropoffLocation, tvConfirmationNo;

        CustomViewHolder(View view) {
            super(view);

            this.imageView = view.findViewById(R.id.realty_image);
            this.tvTitle = view.findViewById(R.id.realty_title);
            this.tvDate = view.findViewById(R.id.realty_date);
            this.tvStart = view.findViewById(R.id.rental_start);
            this.tvEnd = view.findViewById(R.id.rental_end);
            this.tvPrice = view.findViewById(R.id.realty_price);
            this.tvStatus = view.findViewById(R.id.realty_status);
            this.tvSource = view.findViewById(R.id.realty_source);
            this.btnBookCancel = view.findViewById(R.id.btn_book_cancel);
            this.btnBookEdit = view.findViewById(R.id.btn_book_edit);
            this.btnBookConfirm = view.findViewById(R.id.btn_book_confirm);
            this.btnGroupLayout = view.findViewById(R.id.btn_group_layout);
            this.ratingBar = view.findViewById(R.id.ratingBar);

            this.avisLayout = view.findViewById(R.id.avis_layout);
            this.tvPickupLocation = view.findViewById(R.id.pickup_location);
            this.tvDropoffLocation = view.findViewById(R.id.dropoff_location);
            this.tvConfirmationNo = view.findViewById(R.id.confirm_no);
        }
    }

    public interface Listener {
        /**
         * @param position
         */
        void onItemEdit(int position);
        void onItemCancel(int position);
        void onItemClick(int position);
        void onItemConfirm(int position);
    }
}
