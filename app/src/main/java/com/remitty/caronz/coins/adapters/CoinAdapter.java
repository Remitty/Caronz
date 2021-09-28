package com.remitty.caronz.coins.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.CoinModel;
import com.remitty.caronz.utills.SettingsMain;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.MyViewHolder> {

    SettingsMain settingsMain;
    Context context;
    private ArrayList<CoinModel> list;
    private CoinAdapter.Listener listener;

    public CoinAdapter(Context context, ArrayList<CoinModel> Data) {
        this.list = Data;
        settingsMain = new SettingsMain(context);
        this.context = context;
    }

    public interface Listener {
        void onDeposit(int position);
    }

    public void setListener(CoinAdapter.Listener listener) {
        this.listener = listener;
    }

    @Override
    public CoinAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coin, parent, false);
        return new CoinAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CoinAdapter.MyViewHolder holder, final int position) {
        final CoinModel feedItem = list.get(position);
        holder.tvRate.setText(String.format("$%.2f", feedItem.getRate()));
        holder.tvSymbol.setText(feedItem.getSymbol());
        holder.tvName.setText(feedItem.getName());
        holder.btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeposit(position);
            }
        });
        Picasso.with(this.context).load(feedItem.getIcon()).error(this.context.getDrawable(R.drawable.bt_circle_ripple)).placeholder(this.context.getDrawable(R.drawable.bt_circle_ripple)).into(holder.icon);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvRate, tvSymbol, tvName;
        ImageView icon;
        Button btnDeposit;
        MyViewHolder(View v) {
            super(v);

            tvRate = v.findViewById(R.id.tv_rate);
            tvSymbol = v.findViewById(R.id.tv_symbol);
            tvName = v.findViewById(R.id.tv_name);
            icon = v.findViewById(R.id.icon);
            btnDeposit = v.findViewById(R.id.btn_deposit);
        }
    }
}
