package com.example.tesorosdeltiempo.adaptador

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.R
import com.example.tesorosdeltiempo.seguridad.AyArchivoSeguro
import java.io.File
import android.media.MediaMetadataRetriever

// Miniaturas del GridView principal con foto decodificada o icono según el tipo
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
        when (recuerdo.type.trim().uppercase()) {
            "FOTO" -> {
                val bitmap = if (AyArchivoSeguro.esRutaArchivoCifrado(recuerdo.filePath)) {
                    var tempFile: File? = null
                    try {
                        tempFile = AyArchivoSeguro.descifrarAFicheroTemporal(
                            mContext,
                            recuerdo.filePath,
                            "thumb",
                            ".jpg"
                        )
                        BitmapFactory.decodeFile(tempFile!!.absolutePath)
                    } finally {
                        try { tempFile?.delete() } catch (_: Exception) { }
                    }
                } else {
                    BitmapFactory.decodeFile(recuerdo.filePath)
                }
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            }
            "VIDEO" -> {
                // Miniatura real del vídeo (si está cifrado, descifra a temporal para generar thumbnail)
                var tempFile: File? = null
                val bmp = try {
                    val ruta = if (AyArchivoSeguro.esRutaArchivoCifrado(recuerdo.filePath)) {
                        tempFile = AyArchivoSeguro.descifrarAFicheroTemporal(
                            mContext,
                            recuerdo.filePath,
                            "thumb_video",
                            ".mp4"
                        )
                        tempFile!!.absolutePath
                    } else {
                        recuerdo.filePath
                    }
                    val f = File(ruta)
                    if (!f.exists()) {
                        null
                    } else {
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(f.absolutePath)
                            retriever.getFrameAtTime(0) // primer frame
                        } finally {
                            try { retriever.release() } catch (_: Exception) { }
                        }
                    }
                } catch (_: Exception) {
                    null
                } finally {
                    try { tempFile?.delete() } catch (_: Exception) { }
                }

                if (bmp != null) {
                    imageView.setImageBitmap(bmp)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_media_play)
                }
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