package com.remitty.caronz.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.remitty.caronz.R;

import java.util.ArrayList;

public class AmentiesAdapter extends BaseAdapter {
    private ArrayList data;
    private LayoutInflater inflater;

    public AmentiesAdapter(Context context, ArrayList array) {
        data = array;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        final String current = (String) data.get(i);

        if (view == null) {
            view = inflater.inflate(R.layout.item_amenty_check, viewGroup, false);

            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.name);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.name.setText(current);

        return view;
    }

    private class ViewHolder {
        TextView name;
    }
}
