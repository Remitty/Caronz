package com.remitty.caronz.withdraw.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class WithdrawAdapter extends RecyclerView.Adapter<WithdrawAdapter.OrderViewHolder> {

    ArrayList<JSONObject> history;

    public WithdrawAdapter(ArrayList history) {
        this.history = history;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvItemPrice, tvItemStatus, tvItemDate, tvItemReceived, tvItemKind;

        @SuppressLint("ResourceAsColor")
        public OrderViewHolder(View view) {
            super(view);

            tvItemPrice = view.findViewById(R.id.item_price);
            tvItemReceived = view.findViewById(R.id.item_received);
            tvItemKind = view.findViewById(R.id.item_kind);
            tvItemStatus = view.findViewById(R.id.item_status);
            tvItemDate = view.findViewById(R.id.item_date);

        }
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_withdraw_history, parent, false);
        OrderViewHolder vh = new OrderViewHolder(mView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderViewHolder holder, final int position) {
        JSONObject item = history.get(position);
        String kind = item.optString("source");
        String unit = "$";
        holder.tvItemPrice.setText(unit + item.optString("request_amount"));
        holder.tvItemReceived.setText(unit + item.optString("withdraw_amount"));
        holder.tvItemStatus.setText(item.optString("status"));
        holder.tvItemKind.setText(kind);
        holder.tvItemDate.setText(item.optString("updated_at").substring(0, 10));

    }

    @Override
    public int getItemCount() {
        return history != null ? history.size(): 0;
    }

}
