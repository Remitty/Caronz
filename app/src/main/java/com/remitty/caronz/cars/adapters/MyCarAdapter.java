package com.remitty.caronz.cars.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.CarModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyCarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    ArrayList<CarModel> carList;

    Context mContext;
    Listener ItemClickListener;

    public MyCarAdapter(Context context, ArrayList<CarModel> data) {
        this.carList = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_car, parent, false);
            return new CarViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CarViewHolder) {
            populateItemRows((CarViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    private void populateItemRows(CarViewHolder holder, int position) {

        final CarModel item = carList.get(position);
        if(!item.getFirstImage().isEmpty())
        Picasso.with(mContext).load(item.getFirstImage()).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onItemClick(position);
            }
        });
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onEdit(position);
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onDelete(position);
            }
        });
        holder.btnActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onActive(position);
            }
        });
        if(item.isBuy())
            holder.ratingBar.setVisibility(View.GONE);
        holder.ratingBar.setRating(item.getRate());
        LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);
        holder.tvRentPrice.setText("$ " + item.getPrice());
        holder.tvCat.setText(item.getCatName());
        holder.tvName.setText(item.getName());
        holder.tvYear.setText(item.getYear());
        if(item.getStatus().equals("Rental") || item.getStatus().equals("Sold")) {
            holder.orderBtnGroupLayout.setVisibility(View.VISIBLE);
            holder.activeBtnGroupLayout.setVisibility(View.GONE);
        } else {
            holder.orderBtnGroupLayout.setVisibility(View.GONE);
            holder.activeBtnGroupLayout.setVisibility(View.VISIBLE);
        }

        if(item.isHire())
            holder.tvPriceUnit.setText(" /" + item.getUnit());
        if(item.isRental())
            holder.tvPriceUnit.setText("/day");
        if(item.isBuy())
            holder.tvPriceUnit.setText("");

    }

    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return carList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setOnItemClickListener(Listener onItemClickListener) {
        this.ItemClickListener = onItemClickListener;
    }

    public interface Listener {
        /**
         * @param position
         */
        void onItemClick(int position);
        void onEdit(int position);
        void onDelete(int position);
        void onActive(int position);
    }

    private class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView tvName, tvCat, tvRentPrice, tvPriceUnit, tvYear;
        RatingBar ratingBar;
        LinearLayout activeBtnGroupLayout, orderBtnGroupLayout;
        Button btnEdit, btnDelete, btnActive;
        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.car_image);
            tvName = itemView.findViewById(R.id.car_name);
            tvYear = itemView.findViewById(R.id.car_year);
            tvCat = itemView.findViewById(R.id.car_cat);
            tvRentPrice = itemView.findViewById(R.id.car_price);
            tvPriceUnit = itemView.findViewById(R.id.price_unit);
            ratingBar = itemView.findViewById(R.id.car_rate);

            activeBtnGroupLayout = itemView.findViewById(R.id.active_btn_group_layout);
            orderBtnGroupLayout = itemView.findViewById(R.id.order_btn_group_layout);

            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnActive = itemView.findViewById(R.id.btn_active);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(MyCarAdapter.LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }

    public void loadMore(ArrayList<CarModel> newList) {
        carList.retainAll(newList);
        notifyItemRangeChanged(carList.size()-1 , newList.size());
    }
}

