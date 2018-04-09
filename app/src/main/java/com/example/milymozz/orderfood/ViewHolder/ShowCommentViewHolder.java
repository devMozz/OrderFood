package com.example.milymozz.orderfood.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.milymozz.orderfood.R;

/**
 * Created by milymozz on 2018. 3. 8..
 */

public class ShowCommentViewHolder extends RecyclerView.ViewHolder  {
    public TextView txtUserPhone, txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(View itemView) {
        super(itemView);
        txtUserPhone = (TextView) itemView.findViewById(R.id.txtUserPhone);
        txtComment = (TextView) itemView.findViewById(R.id.txtComment);
        ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);

    }

}
