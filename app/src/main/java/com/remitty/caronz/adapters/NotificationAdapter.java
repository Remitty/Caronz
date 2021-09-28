package com.remitty.caronz.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.NotificationModel;
import com.remitty.caronz.payment.adapter.CardListAdapter;
import com.remitty.caronz.utills.SettingsMain;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    SettingsMain settingsMain;
    Context context;
    private ArrayList<NotificationModel> list;
    private Listener listener;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> Data) {
        this.list = Data;
        settingsMain = new SettingsMain(context);
        this.context = context;
    }

    public interface Listener {
        void onDelete(int position);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final NotificationModel feedItem = list.get(position);
        holder.tvDate.setText(feedItem.getDate());
        holder.tvMsg.setText(feedItem.getMsg());
        holder.delete.setOnClickListener(new View.OnClickListener() {
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
        TextView tvMsg, tvDate;
        ImageView delete;
        MyViewHolder(View v) {
            super(v);

            tvMsg = v.findViewById(R.id.tv_msg);
            tvDate = v.findViewById(R.id.tv_date);
            delete = v.findViewById(R.id.img_delete);
        }
    }
}
