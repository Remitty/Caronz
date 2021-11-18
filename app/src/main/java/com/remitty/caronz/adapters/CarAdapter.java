package com.remitty.caronz.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;
import com.remitty.caronz.models.CarModel;
import com.remitty.caronz.models.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    ArrayList<CarModel> carList;

    Context mContext;
    Listener ItemClickListener;

    String service;
    public CarAdapter(Context context, ArrayList<CarModel> data, String service) {
        this.carList = data;
        this.mContext = context;
        this.service = service;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_car, parent, false);
            return new CarAdapter.CarViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new CarAdapter.LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CarAdapter.CarViewHolder) {
            populateItemRows((CarAdapter.CarViewHolder) viewHolder, position);
        } else if (viewHolder instanceof CarAdapter.LoadingViewHolder) {
            showLoadingView((CarAdapter.LoadingViewHolder) viewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    private void populateItemRows(CarAdapter.CarViewHolder holder, int position) {

        final CarModel item = carList.get(position);
        if(item.isHire()) {
            UserModel owner = item.getOwner();
            if(owner != null && !owner.getPicture().isEmpty())
            Picasso.with(mContext).load(owner.getPicture()).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).into(holder.image);
        }
        else {
            if(!item.getFirstImage().isEmpty())
                Picasso.with(mContext).load(item.getFirstImage()).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).into(holder.image);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemClickListener.onItemClick(position);
                }
            });

        }

        holder.ratingBar.setRating(item.getRate());
        LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);

        if(!item.isBuy()) {
            if(item.isRental()) {
                holder.tvPriceView.setText("/day");
            }
            else {
                holder.tvPriceView.setText("/" + " " + item.getUnit());
            }
        }
        else {
            holder.ratingBar.setVisibility(View.GONE);
            holder.tvPriceView.setVisibility(View.GONE);
        }
        holder.tvPrice.setText(item.getPrice());
        holder.tvCurrency.setText(item.getCurrency());
        holder.tvName.setText(item.getCatName() + " " + item.getName());
        holder.tvModelYear.setText(item.getYear());
        holder.ratingBar.setVisibility(View.GONE);
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

    public void setOnItemClickListener(CarAdapter.Listener onItemClickListener) {
        this.ItemClickListener = onItemClickListener;
    }

    public interface Listener {
        /**
         * @param position
         */
        void onItemClick(int position);
    }

    private class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView tvName, tvPrice, tvPriceView, tvCurrency, tvModelYear;
        RatingBar ratingBar;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.car_image);
            tvName = itemView.findViewById(R.id.tv_car_title);
            tvModelYear = itemView.findViewById(R.id.tv_car_model_year);
            tvPrice = itemView.findViewById(R.id.tv_car_price);
            tvCurrency = itemView.findViewById(R.id.tv_currency);
            tvPriceView = itemView.findViewById(R.id.tv_car_price_view);
            ratingBar = itemView.findViewById(R.id.car_rating);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(CarAdapter.LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }

    public void loadMore(ArrayList<CarModel> newList) {
        carList.retainAll(newList);
        notifyItemRangeChanged(carList.size()-1 , newList.size());
    }
}

