package com.example.tesorosdeltiempo

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tesorosdeltiempo.adaptador.GaleriaFotosAdap

class ImagenCompleta : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var galeriaFotosAdapter: GaleriaFotosAdap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_imagen_completa)

        imageView = findViewById(R.id.iv_foto)

        supportActionBar?.title = "Foto Completa"

        val intent: Intent = intent
        val posicion = intent.extras?.getInt("misImagenes") ?: 0

        galeriaFotosAdapter = GaleriaFotosAdap(this)
        imageView.setImageResource(galeriaFotosAdapter.getItem(posicion) as Int)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}