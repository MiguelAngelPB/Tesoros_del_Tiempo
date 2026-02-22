package com.example.tesorosdeltiempo.adaptador

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.tesorosdeltiempo.R

class GaleriaFotosAdap(private val mContext: Context) : BaseAdapter() {

    val imageArray = intArrayOf(
        R.drawable.imagen1,
        R.drawable.imagen2,
        R.drawable.imagen3,
        R.drawable.imagen4,
        R.drawable.imagen5,
        R.drawable.imagen6,
        R.drawable.imagen7,
        R.drawable.imagen8,
        R.drawable.imagen9,
        R.drawable.imagen10,
        R.drawable.imagen11,
        R.drawable.imagen12,
        R.drawable.imagen13,
        R.drawable.imagen14,
        R.drawable.imagen15,
        R.drawable.imagen16,
        R.drawable.imagen17,
        R.drawable.imagen18,
        R.drawable.imagen19,
        R.drawable.imagen20,
        R.drawable.imagen21,
        R.drawable.imagen22,

        )

    override fun getCount(): Int = imageArray.size

    override fun getItem(position: Int): Any = imageArray[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val imageView = ImageView(mContext)
        imageView.setImageResource(imageArray[position])
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        val columnCount = 3
        val size = parent!!.width / columnCount

        imageView.layoutParams = AbsListView.LayoutParams(size, size)

        return imageView
    }
}