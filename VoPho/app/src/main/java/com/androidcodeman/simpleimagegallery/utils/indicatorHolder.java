package com.androidcodeman.simpleimagegallery.utils;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidcodeman.simpleimagegallery.R;


public class indicatorHolder extends RecyclerView.ViewHolder {

    public ImageView image;
    View positionController;
    private CardView card;

    indicatorHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.imageIndicator);
        card = itemView.findViewById(R.id.indicatorCard);
        positionController = itemView.findViewById(R.id.activeImage);
    }
}
