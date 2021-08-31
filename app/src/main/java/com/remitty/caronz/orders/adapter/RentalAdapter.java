package com.remitty.caronz.orders.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private int total = 0;

    public RentalAdapter(Context context, List<RentalModel> realtyList, int total) {
        this.realtyList = realtyList;
        this.mContext = context;
        this.total = total;
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

        customViewHolder.tvAddress.setText(feedItem.getProcessdate());
            customViewHolder.tvPrice.setText("$ " + feedItem.getBookTotal());
            customViewHolder.tvTitle.setText(feedItem.getCar().getName());
            customViewHolder.tvStatus.setText(feedItem.getBookStatus());
//        if(total == 0)
//            customViewHolder.tvPrice.setText("$ " + feedItem.getBookTotal());
//        else {
//            BigDecimal price = new BigDecimal(feedItem.getPrice()).multiply(new BigDecimal(feedItem.getBookDuration()));
//            BigDecimal fee = new BigDecimal(feedItem.getFee()).multiply(price);
//            customViewHolder.tvPrice.setText("$ " + price.toString() + " ( -" + fee.toString() + ")");
//        }
        customViewHolder.tvTerm.setText(feedItem.getBookFrom() + " - " + feedItem.getBookTo());
//        customViewHolder.tvStatus.setText(feedItem.getBookStatus());
//
//        if(feedItem.getBookStatus() == 1) {
//            customViewHolder.tvStatus.setVisibility(View.GONE);
//        }

        Picasso.with(mContext).load(feedItem.getCar().getFirstImage())
                .resize(270, 270).centerCrop()
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(customViewHolder.imageView);

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

        if(show) {
            customViewHolder.btnGroupLayout.setVisibility(View.VISIBLE);
        }

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
        ImageView imageView;
        TextView tvTitle, tvAddress, tvTerm, tvPrice, tvStatus;
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
            this.btnBookCancel = view.findViewById(R.id.btn_book_cancel);
            this.btnBookEdit = view.findViewById(R.id.btn_book_edit);
            this.btnBookConfirm = view.findViewById(R.id.btn_book_confirm);
            this.btnGroupLayout = view.findViewById(R.id.btn_group_layout);
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
