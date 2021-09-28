package com.remitty.caronz.avis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.avis.models.AvisCar;
import com.remitty.caronz.utills.SettingsMain;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AvisCarAdapter extends RecyclerView.Adapter<AvisCarAdapter.MyViewHolder> {

    SettingsMain settingsMain;
    Context context;
    private ArrayList<AvisCar> list;
    Listener listener;

    public AvisCarAdapter(Context context, ArrayList<AvisCar> Data) {
        this.list = Data;
        settingsMain = new SettingsMain(context);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_avis_car, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final AvisCar feedItem = list.get(position);
        holder.tvPrice.setText(feedItem.getPrice());
        holder.tvCurrency.setText(feedItem.getCurrency());
        holder.tvName.setText(feedItem.getModel());
        Picasso.with(context).load(feedItem.getCatImage()).placeholder(R.drawable.placeholder).into(holder.carImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface Listener {
        void onItemClick(int position);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrice, tvCurrency, tvName;
        ImageView carImage;
        MyViewHolder(View v) {
            super(v);

            tvPrice = v.findViewById(R.id.tv_car_price);
            tvCurrency = v.findViewById(R.id.tv_currency);
            tvName = v.findViewById(R.id.tv_car_title);
            carImage = v.findViewById(R.id.car_image);
        }
    }
}

