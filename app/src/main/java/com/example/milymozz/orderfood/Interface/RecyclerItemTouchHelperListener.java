package com.example.milymozz.orderfood.Interface;

import android.support.v7.widget.RecyclerView;

/**
 * Created by milymozz on 2018. 3. 21..
 */

public interface RecyclerItemTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
