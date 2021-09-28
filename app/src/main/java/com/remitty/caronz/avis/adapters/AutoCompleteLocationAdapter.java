package com.remitty.caronz.avis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.remitty.caronz.R;
import com.remitty.caronz.avis.models.AvisLocation;

import java.util.ArrayList;

public class AutoCompleteLocationAdapter extends ArrayAdapter<AvisLocation> {
    ViewHolder holder;
    Context context;
    ArrayList<AvisLocation> Places;

    public AutoCompleteLocationAdapter(Context context, ArrayList<AvisLocation> modelsArrayList) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.Places = modelsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.autocomplete_row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.place_name);
            holder.location = (TextView) rowView.findViewById(R.id.place_detail);
            holder.imgRecent = (ImageView) rowView.findViewById(R.id.imgRecent);
            rowView.setTag(holder);
        } else
            holder = (ViewHolder) rowView.getTag();

        holder.Place = Places.get(position);

        holder.imgRecent.setImageResource(R.drawable.location_search);


        holder.location.setText(holder.Place.getAddress());
        holder.name.setText(holder.Place.getName() + "(" + holder.Place.getHours() + ")");
        return rowView;
    }

    class ViewHolder {
        AvisLocation Place;
        TextView location, name;
        ImageView imgRecent;
    }

    @Override
    public int getCount(){
        return Places.size();
    }
}
