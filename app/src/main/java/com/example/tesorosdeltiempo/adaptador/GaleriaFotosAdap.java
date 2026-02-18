package com.example.tesorosdeltiempo.adaptador;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.example.tesorosdeltiempo.R;

public class GaleriaFotosAdap extends BaseAdapter {
    private Context mContext;
    public int[] imageArray = {
            R.drawable.ImPrueba,
            R.drawable.ImPrueba2
    };

    public GaleriaFotosAdap(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return imageArray.length;
    }

    @Override
    public Object getItem(int position) {
        return imageArray[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(imageArray[position]);
        imageView.setScaleType((ImageView.ScaleType.CENTER_CROP));
        // w:340 y h:350
        imageView.setLayoutParams(new GridLayout.LayoutParams(340, 350) );

        return imageView;
    }
}
