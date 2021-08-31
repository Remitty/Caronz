package com.remitty.caronz.helper;


import android.util.Log;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessNestedScrollViewListener implements NestedScrollView.OnScrollChangeListener {

    private boolean loading = true;
    private int pageNumber = 0;
    private int previousTotalItemCount = 0;
    private int startingPageIndex = 0;

    RecyclerView.LayoutManager mLayoutManager;


    public EndlessNestedScrollViewListener(RecyclerView.LayoutManager layoutManager){
        this.mLayoutManager = layoutManager;
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY,
                               int oldScrollX, int oldScrollY) {

        int totalItemCount = mLayoutManager.getItemCount();

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.pageNumber = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the active page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        if (scrollY > oldScrollY) {
            Log.i("nested scroll", "Scroll DOWN");
        }
        if (scrollY < oldScrollY) {
            Log.i("nested scroll", "Scroll UP");
        }

        if (scrollY == 0) {
            Log.i("nested scroll", "TOP SCROLL");
        }

        if (scrollY == ( v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight() )) {
            Log.i("nested scroll", "BOTTOM SCROLL");
            // here where the trick is going
            if (!loading){
                pageNumber++;
                // calling from adapter addToExistingList(list)
                // with the defined Adapter instance
                onLoadMore(pageNumber);
                // reset the boolean(loading) to prevent
                // auto loading data from APi
                loading = true;

            }
        }

    }

    public abstract void onLoadMore(int page);
}
