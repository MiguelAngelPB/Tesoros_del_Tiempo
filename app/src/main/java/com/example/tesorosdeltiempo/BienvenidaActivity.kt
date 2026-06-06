package com.example.tesorosdeltiempo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.tesorosdeltiempo.seguridad.BiometricaDesbloqueoAy

// Pantalla inicial con logo y desbloqueo biométrico (o PIN del sistema) antes de la galería
class BienvenidaActivity : AppCompatActivity() {

    private var irHecho = false
    private var trabajoRetraso: Job? = null
    private var esperandoBiometrica = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        irHecho = savedInstanceState?.getBoolean(STATE_IR_HECHO) == true
        setContentView(R.layout.fondo_inicio)

        if (irHecho) {
            continuarTrasSplash()
            return
        }

        findViewById<android.view.View>(android.R.id.content).setOnClickListener {
            continuarTrasSplash()
        }

        trabajoRetraso = lifecycleScope.launch {
            delay(2200)
            continuarTrasSplash()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_IR_HECHO, irHecho)
    }

    private fun continuarTrasSplash() {
        if (irHecho || isFinishing || isDestroyed || esperandoBiometrica) return
        trabajoRetraso?.cancel()
        esperandoBiometrica = true

        BiometricaDesbloqueoAy.solicitarAlAbrir(
            activity = this,
            alDesbloquear = {
                esperandoBiometrica = false
                irAMain()
            },
            alCancelar = {
                esperandoBiometrica = false
            },
            alSinMetodoDesbloqueo = {
                esperandoBiometrica = false
                Toast.makeText(
                    this,
                    getString(R.string.bio_sin_bloqueo_sistema),
                    Toast.LENGTH_LONG
                ).show()
                finishAffinity()
            }
        )
    }

    private fun irAMain() {
        if (irHecho || isFinishing || isDestroyed) return
        irHecho = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val STATE_IR_HECHO = "ir_hecho"
    }
}