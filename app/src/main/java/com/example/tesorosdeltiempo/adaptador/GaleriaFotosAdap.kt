package com.example.tesorosdeltiempo.adaptador

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import com.example.tesorosdeltiempo.R

class GaleriaFotosAdap(private val mContext: Context) : BaseAdapter() {

    private val imageArray = intArrayOf(
        R.drawable.imprueba,
        R.drawable.imprueba2
    )

    override fun getCount(): Int {
        return imageArray.size
    }

    override fun getItem(position: Int): Any {
        return imageArray[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val imageView = ImageView(mContext)
        imageView.setImageResource(imageArray[position])
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        return imageView
    }
}
