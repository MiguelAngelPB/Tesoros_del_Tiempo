package com.example.tesorosdeltiempo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Pantalla inicial con logo antes de MainActivity

class BienvenidaActivity : AppCompatActivity() {

    private var irHecho = false
    private var trabajoRetraso: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fondo_inicio)

        findViewById<android.view.View>(android.R.id.content).setOnClickListener {
            irAMain()
        }

        trabajoRetraso = lifecycleScope.launch {
            delay(2200)
            irAMain()
        }
    }

    private fun irAMain() {
        if (irHecho || isFinishing) return
        irHecho = true
        trabajoRetraso?.cancel()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}