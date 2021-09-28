package com.remitty.caronz.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

public class DriverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    ArrayList<CarModel> carList;

    Context mContext;
    Listener ItemClickListener;

    public DriverAdapter(Context context, ArrayList<CarModel> data) {
        this.carList = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_driver, parent, false);
            return new DriverAdapter.CarViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new DriverAdapter.LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof DriverAdapter.CarViewHolder) {
            populateItemRows((DriverAdapter.CarViewHolder) viewHolder, position);
        } else if (viewHolder instanceof DriverAdapter.LoadingViewHolder) {
            showLoadingView((DriverAdapter.LoadingViewHolder) viewHolder, position);
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    private void populateItemRows(DriverAdapter.CarViewHolder holder, int position) {

        final CarModel item = carList.get(position);
        UserModel owner = item.getOwner();
        if(!item.getFirstImage().isEmpty())
            Picasso.with(mContext).load(owner.getPicture()).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).into(holder.image);
        holder.btnHire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onHire(position);
            }
        });
        holder.btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onView(position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemClickListener.onView(position);
            }
        });

        holder.ratingBar.setRating(item.getRate());
        LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#ffcc00"), PorterDuff.Mode.SRC_ATOP);

        holder.tvPriceView.setText(" /" + item.getUnit());
        holder.tvPrice.setText("$ " + item.getPrice());
        holder.tvName.setText(owner.getUserName());


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

    public void setOnItemClickListener(DriverAdapter.Listener onItemClickListener) {
        this.ItemClickListener = onItemClickListener;
    }

    public interface Listener {
        /**
         * @param position
         */
        void onHire(int position);
        void onView(int position);
    }

    private class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView tvName, tvPrice, tvPriceView;
        RatingBar ratingBar;
        Button btnHire;
        ImageButton btnView;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.driver_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_car_price);
            tvPriceView = itemView.findViewById(R.id.tv_car_price_view);
            ratingBar = itemView.findViewById(R.id.car_rating);
            btnHire = itemView.findViewById(R.id.btn_hire);
            btnView = itemView.findViewById(R.id.btn_view);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(DriverAdapter.LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }

    public void loadMore(ArrayList<CarModel> newList) {
        carList.retainAll(newList);
        notifyItemRangeChanged(carList.size()-1 , newList.size());
    }
}
