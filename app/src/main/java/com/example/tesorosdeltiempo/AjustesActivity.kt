package com.example.tesorosdeltiempo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tesorosdeltiempo.ui.BarraArribaAy

class AjustesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)
        // Barra superior fija
        BarraArribaAy.ponerBarraArriba(this)
    }
}

