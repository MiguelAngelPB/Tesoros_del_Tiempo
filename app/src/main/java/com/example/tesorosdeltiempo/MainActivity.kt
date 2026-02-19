package com.example.tesorosdeltiempo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.example.tesorosdeltiempo.adaptador.GaleriaFotosAdap

class MainActivity : AppCompatActivity() {

    private lateinit var gridView: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById(R.id.Id_Galeria)
        gridView.adapter = GaleriaFotosAdap(this)

        gridView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->

                val intent = Intent(this, ImagenCompleta::class.java)
                intent.putExtra("misImagenes", position)
                startActivity(intent)
            }
    }
}