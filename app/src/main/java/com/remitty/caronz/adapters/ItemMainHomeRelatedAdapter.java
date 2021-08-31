package com.remitty.caronz.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.remitty.caronz.R;
import com.remitty.caronz.helper.OnItemClickListener2;
import com.remitty.caronz.models.RentalModel;
import com.remitty.caronz.utills.SettingsMain;


public class ItemMainHomeRelatedAdapter extends RecyclerView.Adapter<ItemMainHomeRelatedAdapter.MyViewHolder> {

    SettingsMain settingsMain;
    Context context;
    private ArrayList<RentalModel> list;
    private OnItemClickListener2 onItemClickListener;

    public ItemMainHomeRelatedAdapter(Context context, ArrayList<RentalModel> Data) {
        this.list = Data;
        settingsMain = new SettingsMain(context);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main_home_related, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final RentalModel feedItem = list.get(position);
//
//
//
//        if (!TextUtils.isEmpty(feedItem.getImageResourceId())) {
//            Picasso.with(context).load(feedItem.getImageResourceId())
//                    .resize(250, 250).centerCrop()
//                    .error(R.drawable.placeholder)
//                    .placeholder(R.drawable.placeholder)
//                    .into(holder.mainImage);
//        }
//        View.OnClickListener listener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onItemClickListener.onItemClick(feedItem);
//            }
//        };

//        holder.linearLayout.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnItemClickListener2 onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTV, priceTV, locationTV, featureText;
        ImageView mainImage;
        RelativeLayout linearLayout;

        MyViewHolder(View v) {
            super(v);

            titleTextView = v.findViewById(R.id.text_view_name);
            dateTV = v.findViewById(R.id.date);
            priceTV = v.findViewById(R.id.prices);
            locationTV = v.findViewById(R.id.location);
            priceTV.setTextColor(Color.parseColor(settingsMain.getMainColor()));

            mainImage = v.findViewById(R.id.image_view);

            linearLayout = v.findViewById(R.id.linear_layout_card_view);
            featureText = v.findViewById(R.id.textView4);
        }
    }
}
