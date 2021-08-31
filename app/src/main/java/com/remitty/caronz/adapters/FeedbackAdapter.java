package com.remitty.caronz.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.CarFeedback;
import com.remitty.caronz.utills.SettingsMain;

import java.util.ArrayList;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.MyViewHolder> {

    SettingsMain settingsMain;
    Context context;
    private ArrayList<CarFeedback> list;

    public FeedbackAdapter(Context context, ArrayList<CarFeedback> Data) {
        this.list = Data;
        settingsMain = new SettingsMain(context);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final CarFeedback feedItem = list.get(position);
        holder.ratingBar.setRating(feedItem.getRate());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvComment, tvRate;
        RatingBar ratingBar;
        MyViewHolder(View v) {
            super(v);

            tvComment = v.findViewById(R.id.tv_comment);
            tvRate = v.findViewById(R.id.tv_rate);
            ratingBar = v.findViewById(R.id.ratingBar);
            LayerDrawable stars = (LayerDrawable) this.ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);
        }
    }
}

