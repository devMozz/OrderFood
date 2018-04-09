package com.example.milymozz.orderfood.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.milymozz.orderfood.Interface.ItemClickListener;
import com.example.milymozz.orderfood.R;

/**
 * Created by milymozz on 2018. 2. 8..
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView food_name, food_price;
    public ImageView food_image, fav_image, share_image, quickCart;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(View itemView) {
        super(itemView);

        food_name = (TextView) itemView.findViewById(R.id.food_name);
        food_image = (ImageView) itemView.findViewById(R.id.food_image);
        fav_image = (ImageView) itemView.findViewById(R.id.fav);
        share_image = (ImageView) itemView.findViewById(R.id.btnShare);
        food_price = (TextView) itemView.findViewById(R.id.food_price);
        quickCart = (ImageView) itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
