package com.remitty.caronz.hire.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Toast;

import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.models.HireModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.remitty.caronz.R;
import com.remitty.caronz.utills.SettingsMain;

public class HireAdapter extends RecyclerView.Adapter<HireAdapter.CustomViewHolder> {

    SettingsMain settingsMain;
    private List<HireModel> realtyList = new ArrayList<>();
    private Context mContext;
    private Listener ItemClickListener;
    private boolean show = false;

    public HireAdapter(Context context, List<HireModel> realtyList) {
        this.realtyList = realtyList;
        this.mContext = context;
        settingsMain = new SettingsMain(context);

    }

    public HireAdapter(Context context, List<HireModel> realtyList, boolean flag) {
        this.realtyList = realtyList;
        this.mContext = context;
        this.show = flag;
        settingsMain = new SettingsMain(context);

    }

    @Override
    public HireAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_hire, viewGroup, false);
        return new HireAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HireAdapter.CustomViewHolder customViewHolder, int i) {
        final HireModel feedItem = realtyList.get(i);
        CarModel car = feedItem.getCar();

        customViewHolder.tvAddress.setText(feedItem.getProcessDate());
        customViewHolder.tvPrice.setText(car.getCurrency() + feedItem.getBookTotal());
        if(feedItem.getOwner() != null)
            customViewHolder.tvTitle.setText(feedItem.getOwner().getUserName());
        else
            customViewHolder.tvTitle.setText(car.getCatName() + " " + car.getName());
        customViewHolder.tvStatus.setText(feedItem.getBookStatus());
        customViewHolder.tvTerm.setText(feedItem.getBookFrom());
        customViewHolder.tvPickup.setText(feedItem.getPickupLocation());
        customViewHolder.tvDropoff.setText(feedItem.getDropoffLocation());
        customViewHolder.tvEstDistance.setText(feedItem.getEstDistance());
        customViewHolder.tvEstTime.setText(feedItem.getEstTime());
        customViewHolder.tvPayment.setText(feedItem.getPayment());



        if(feedItem.getOwner() != null) {
            if(!feedItem.getOwner().getPicture().isEmpty())
                Picasso.with(mContext).load(feedItem.getOwner().getPicture())
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(customViewHolder.imageView);
        } else {
            Picasso.with(mContext).load(car.getFirstImage())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }

        if(!show) {
            customViewHolder.btnGroupLayout.setVisibility(View.GONE);
        }

        if(!feedItem.getBookStatus().equals("Completed")) {
            customViewHolder.ratingBar.setVisibility(View.GONE);
        } else {
            customViewHolder.ratingBar.setRating(feedItem.getRate());
            LayerDrawable stars = (LayerDrawable) customViewHolder.ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);
        }

        if(feedItem.getBookStatus().equals("Hired") || feedItem.getBookStatus().equals("Confirmed"))
            customViewHolder.imgDelete.setVisibility(View.GONE);

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

        customViewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemRemove(i);
            }
        });

        customViewHolder.itemView.setOnClickListener(listener);

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

    public Listener getOnItemClickListener() {
        return ItemClickListener;
    }

    public void setOnItemClickListener(Listener onItemClickListener) {
        this.ItemClickListener = onItemClickListener;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private final RatingBar ratingBar;
        ImageView imageView, copyPickup, copyDropOff, imgDelete;
        TextView tvTitle, tvAddress, tvTerm, tvPrice, tvStatus, tvPickup, tvDropoff, tvEstTime, tvEstDistance, tvPayment;
        Button btnBookEdit, btnBookCancel, btnBookConfirm;
        LinearLayout btnGroupLayout;

        CustomViewHolder(View view) {
            super(view);

            this.imageView = view.findViewById(R.id.realty_image);
            this.tvTitle = view.findViewById(R.id.realty_title);
            this.tvAddress = view.findViewById(R.id.realty_date);
            this.tvTerm = view.findViewById(R.id.realty_term);
            this.tvPrice = view.findViewById(R.id.realty_price);
            this.tvStatus = view.findViewById(R.id.realty_status);
            this.tvPayment = view.findViewById(R.id.realty_source);
            this.btnBookCancel = view.findViewById(R.id.btn_book_cancel);
            this.btnBookEdit = view.findViewById(R.id.btn_book_edit);
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
        void onItemEdit(int position);
        void onItemCancel(int position);
        void onItemRemove(int position);
        void onItemClick(int position);
        void onItemConfirm(int position);
    }
}
