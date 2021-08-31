package com.remitty.caronz.home.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.remitty.caronz.models.HomeCatListModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.remitty.caronz.R;
import com.remitty.caronz.helper.OnItemClickListener;

public class ItemMainAllCatAdapter extends RecyclerView.Adapter<ItemMainAllCatAdapter.CustomViewHolder> {
    private ArrayList<HomeCatListModel> feedItemList;
    private Context mContext;
    private OnItemClickListener onItemClickListener;
    private int noOfCol;

    public ItemMainAllCatAdapter(Context context, ArrayList<HomeCatListModel> feedItemList, int noOFCol) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.noOfCol = noOFCol;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main_home_allcat, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {

//        int dimensionInPixel = 90;
//
//        if (noOfCol == 2) {
//            dimensionInPixel = 110;
//        }
//        if (noOfCol == 3) {
//            dimensionInPixel = 90;
//        }
//        if (noOfCol == 4) {
//            dimensionInPixel = 70;
//        }

//       int dimensionInPixel = 60;
//
//        customViewHolder.imageView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInPixel, mContext.getResources().getDisplayMetrics());

        final HomeCatListModel feedItem = feedItemList.get(i);

        //Download image using picasso library
        if (!TextUtils.isEmpty(feedItem.getThumbnail())) {
            Picasso.with(mContext).load(feedItem.getThumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }
        //Setting text view title
//        customViewHolder.layout.setBackgroundColor(Color.parseColor(feedItem.getBGColor()));
        customViewHolder.textView.setText(feedItem.getTitle());
//        customViewHolder.textView.setTextColor(WHITE);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(feedItem);
            }
        };
        customViewHolder.imageView.setOnClickListener(listener);
        customViewHolder.textView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return feedItemList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        RelativeLayout layout;
        CustomViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.thumbnail);
            this.textView = view.findViewById(R.id.title);
            this.layout = view.findViewById(R.id.parentView);
        }
    }
}