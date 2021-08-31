package com.remitty.caronz.home.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.remitty.caronz.R;
import com.remitty.caronz.helper.OnItemClickListener;
import com.remitty.caronz.home.helper.CustomCategoriesFilter;
import com.remitty.caronz.models.HomeCatListModel;
import com.remitty.caronz.utills.SettingsMain;
import com.squareup.picasso.Picasso;

public class ItemHomeAllCategoriesAdapter extends RecyclerView.Adapter<ItemHomeAllCategoriesAdapter.CustomViewHolder>
        implements Filterable {

    public List<HomeCatListModel> feedItemList;
    public ArrayList<HomeCatListModel> feedItemListFiltered;
    SettingsMain settingsMain;
    CustomCategoriesFilter filter;
    private Context mContext;
    private OnItemClickListener oNItemClickListener;

    public ItemHomeAllCategoriesAdapter(Context context, ArrayList<HomeCatListModel> feedItemList) {
        this.feedItemList = feedItemList;
        this.feedItemListFiltered = feedItemList;
        this.mContext = context;
        settingsMain = new SettingsMain(context);

    }

    @Override
    public ItemHomeAllCategoriesAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_viewall_categories, null);
        return new ItemHomeAllCategoriesAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHomeAllCategoriesAdapter.CustomViewHolder customViewHolder, int i) {
        final HomeCatListModel feedItem = feedItemList.get(i);
        customViewHolder.tv_cat_value.setText(feedItem.getTitle());
        Picasso.with(mContext).load(feedItem.getThumbnail()).into(customViewHolder.catImage);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oNItemClickListener.onItemClick(feedItem);
            }
        };

        customViewHolder.view.setOnClickListener(listener);
        customViewHolder.imageView.setBackgroundResource(R.drawable.ic_right_arrow_angle);


    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    public OnItemClickListener getOnItemClickListener() {
        return oNItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.oNItemClickListener = onItemClickListener;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomCategoriesFilter(feedItemListFiltered, this);
        }
        return filter;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView tv_cat_value;
        ImageView imageView, catImage;
        RelativeLayout view;

        CustomViewHolder(View view) {
            super(view);

            this.view = view.findViewById(R.id.layoutCategories);
            this.tv_cat_value = view.findViewById(R.id.tv_cat_value);
            this.imageView = view.findViewById(R.id.imageView);
            this.catImage = view.findViewById(R.id.cat_image);
        }
    }
}
