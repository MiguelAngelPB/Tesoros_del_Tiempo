package com.example.tesorosdeltiempo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ImagenCompleta : AppCompatActivity() {

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_imagen_completa)

        imageView = findViewById(R.id.iv_foto)

        supportActionBar?.title = "Foto Completa"

        val filePath = intent.getStringExtra("filePath")
        val type = intent.getStringExtra("type") ?: "FOTO"

        if (type == "FOTO" && !filePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
