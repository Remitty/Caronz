package com.remitty.caronz.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.remitty.caronz.R;

import org.json.JSONArray;

public class RecentPlacesAdapter extends RecyclerView.Adapter<RecentPlacesAdapter.MyViewHolder> {
    JSONArray jsonArray;
    private Listener listener;

    public RecentPlacesAdapter(JSONArray array) {
        this.jsonArray = array;
    }

    @Override
    public RecentPlacesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.autocomplete_row, parent, false);
        return new RecentPlacesAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecentPlacesAdapter.MyViewHolder holder, int position) {
        String[] name = jsonArray.optJSONObject(position).optString("address").split(",");
        if (name.length > 0) {
            holder.name.setText(name[0]);
        } else {
            holder.name.setText(jsonArray.optJSONObject(position).optString("address"));
        }
        holder.location.setText(jsonArray.optJSONObject(position).optString("address"));

        holder.imgRecent.setImageResource(R.drawable.recent_search);

        holder.lnrLocation.setTag(position);

        holder.lnrLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = Integer.parseInt(view.getTag().toString());
                listener.onClick(pos);
            }
        });
    }

    public interface Listener {
        void onClick(int position);
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, location;

        LinearLayout lnrLocation;

        ImageView imgRecent;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.place_name);
            location = (TextView) itemView.findViewById(R.id.place_detail);
            lnrLocation = (LinearLayout) itemView.findViewById(R.id.lnrLocation);
            imgRecent = (ImageView) itemView.findViewById(R.id.imgRecent);
        }
    }
}
