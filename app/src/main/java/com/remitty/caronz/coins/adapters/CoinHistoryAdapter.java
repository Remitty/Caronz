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
import com.remitty.caronz.models.CoinActivityModel;
import com.remitty.caronz.utills.SettingsMain;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class CoinHistoryAdapter extends RecyclerView.Adapter<CoinHistoryAdapter.MyViewHolder> {

    SettingsMain settingsMain;
    Context context;
    private ArrayList<CoinActivityModel> list;
    private CoinHistoryAdapter.Listener listener;

    public CoinHistoryAdapter(Context context, ArrayList<CoinActivityModel> Data) {
        this.list = Data;
        settingsMain = new SettingsMain(context);
        this.context = context;
    }

    public interface Listener {
        void onDelete(int position);
    }

    public void setListener(CoinHistoryAdapter.Listener listener) {
        this.listener = listener;
    }

    @Override
    public CoinHistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coin_history, parent, false);
        return new CoinHistoryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CoinHistoryAdapter.MyViewHolder holder, final int position) {
        final CoinActivityModel feedItem = list.get(position);
        holder.tvAmount.setText(new DecimalFormat("#,###.########").format(feedItem.getAmount()));
        holder.tvSymbol.setText(feedItem.getSymbol());
        holder.tvAddress.setText(feedItem.getAddress());
        holder.tvDate.setText(feedItem.getDate());
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvSymbol, tvDate, tvAddress;
        ImageView btnDelete;
        MyViewHolder(View v) {
            super(v);

            tvAmount = v.findViewById(R.id.tv_amount);
            tvDate = v.findViewById(R.id.tv_date);
            tvSymbol = v.findViewById(R.id.tv_symbol);
            tvAddress = v.findViewById(R.id.tv_address);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
