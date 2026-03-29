package com.example.tesorosdeltiempo.adaptador

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity

class GaleriaFotosAdap(private val mContext: Context) : BaseAdapter() {

    private var recuerdos: List<RecuerdosEntity> = emptyList()

    fun submitList(nuevosRecuerdos: List<RecuerdosEntity>) {
        recuerdos = nuevosRecuerdos
        notifyDataSetChanged()
    }

    override fun getCount(): Int = recuerdos.size

    override fun getItem(position: Int): Any = recuerdos[position]

    override fun getItemId(position: Int): Long = recuerdos[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = (convertView as? ImageView) ?: ImageView(mContext)
        val recuerdo = recuerdos[position]

        when (recuerdo.type) {
            "FOTO" -> {
                val bitmap = BitmapFactory.decodeFile(recuerdo.filePath)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            }
            "VIDEO" -> {
                imageView.setImageResource(android.R.drawable.ic_media_play)
            }
            "AUDIO" -> {
                imageView.setImageResource(android.R.drawable.ic_btn_speak_now)
            }
            "TEXTO" -> {
                imageView.setImageResource(android.R.drawable.ic_menu_edit)
            }
            else -> {
                imageView.setImageResource(android.R.drawable.ic_menu_help)
            }
        }

        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        val columnCount = 3
        val size = parent!!.width / columnCount

        imageView.layoutParams = AbsListView.LayoutParams(size, size)

        return imageView
    }
}