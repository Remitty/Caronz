package com.remitty.caronz.home.helper;

import android.widget.Filter;

import java.util.ArrayList;

import com.remitty.caronz.home.adapter.ItemHomeAllCategoriesAdapter;
import com.remitty.caronz.models.HomeCatListModel;

public class CustomCategoriesFilter extends Filter {

    private ItemHomeAllCategoriesAdapter adapter;
    private ArrayList<HomeCatListModel> filterList;

    public CustomCategoriesFilter(ArrayList<HomeCatListModel> filterList, ItemHomeAllCategoriesAdapter adapter) {
        this.adapter = adapter;
        this.filterList = filterList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        //CHECK CONSTRAINT VALIDITY
        if (constraint != null && constraint.length() > 0) {
            //CHANGE TO UPPER
            constraint = constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<HomeCatListModel> filteredPlayers = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                //CHECK
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }
            }

            results.count = filteredPlayers.size();
            results.values = filteredPlayers;
        } else {
            results.count = filterList.size();
            results.values = filterList;

        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.feedItemList = (ArrayList<HomeCatListModel>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();
    }
}
