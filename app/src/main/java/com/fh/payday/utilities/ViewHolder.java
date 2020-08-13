package com.fh.payday.utilities;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ViewHolder extends RecyclerView.ViewHolder{

public LinearLayout linearLayout;
public TextView textView;
public ImageView imageView;

public ViewHolder(View itemView, int height, int rows) {
        super(itemView);
        this.itemView.setMinimumHeight(height / rows);
        }

public ViewHolder(View itemView) {
        super(itemView);
        }
        }
