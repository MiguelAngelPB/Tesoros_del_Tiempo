package com.example.tesorosdeltiempo

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tesorosdeltiempo.seguridad.CuidadorAuthManager
import com.example.tesorosdeltiempo.ui.BarraArribaAy

class AjustesActivity : AppCompatActivity() {

    private lateinit var auth: CuidadorAuthManager
    private lateinit var tvEstadoCuidador: TextView
    private lateinit var btnLoginCuidador: Button
    private lateinit var btnRegistroCuidador: Button
    private lateinit var btnLogoutCuidador: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)
        // Barra superior fija
        BarraArribaAy.ponerBarraArriba(this)

        auth = CuidadorAuthManager(this)
        tvEstadoCuidador = findViewById(R.id.tvEstadoCuidador)
        btnLoginCuidador = findViewById(R.id.btnLoginCuidador)
        btnRegistroCuidador = findViewById(R.id.btnRegistroCuidador)
        btnLogoutCuidador = findViewById(R.id.btnLogoutCuidador)

        btnLoginCuidador.setOnClickListener {
            mostrarDialogoAuth(modo = ModoAuth.LOGIN)
        }

        btnRegistroCuidador.setOnClickListener {
            mostrarDialogoAuth(modo = ModoAuth.REGISTRO)
        }

        btnLogoutCuidador.setOnClickListener {
            auth.cerrarSesion()
            actualizarEstadoUI()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }

        actualizarEstadoUI()
    }

    private fun actualizarEstadoUI() {
        val loggedIn = auth.estaConectado()

        tvEstadoCuidador.text = if (loggedIn) {
            val user = auth.obtenerUsuarioGuardado().orEmpty()
            "Cuidador conectado: $user"
        } else {
            "No has iniciado sesión como cuidador"
        }

        val showLoginRegistro = !loggedIn
        btnLoginCuidador.isEnabled = showLoginRegistro
        btnRegistroCuidador.isEnabled = showLoginRegistro
        btnLoginCuidador.alpha = if (showLoginRegistro) 1f else 0.5f
        btnRegistroCuidador.alpha = if (showLoginRegistro) 1f else 0.5f

        btnLogoutCuidador.visibility = if (loggedIn) android.view.View.VISIBLE else android.view.View.GONE
    }

    private enum class ModoAuth { LOGIN, REGISTRO }

    private fun mostrarDialogoAuth(modo: ModoAuth) {
        // Layout
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }

        val etUsername = EditText(this).apply {
            hint = "Usuario (cuidador)"
        }
        val etPassword = EditText(this).apply {
            hint = "Contraseña"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        contenedor.addView(etUsername)
        contenedor.addView(etPassword)

        val titulo = when (modo) {
            ModoAuth.LOGIN -> "Iniciar sesión"
            ModoAuth.REGISTRO -> "Registrarse"
        }

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(contenedor)
            .setPositiveButton(if (modo == ModoAuth.LOGIN) "Entrar" else "Guardar") { _, _ ->
                val user = etUsername.text?.toString().orEmpty()
                val pass = etPassword.text?.toString().orEmpty()

                val ok = if (modo == ModoAuth.LOGIN) {
                    auth.iniciarSesion(user, pass)
                } else {
                    auth.registrar(user, pass)
                }

                if (ok) {
                    Toast.makeText(this, "¡Listo!", Toast.LENGTH_SHORT).show()
                    actualizarEstadoUI()
                } else {
                    val msg = if (modo == ModoAuth.LOGIN) {
                        "Usuario o contraseña incorrectos."
                    } else {
                        "Ya existe un cuidador registrado o los datos no son válidos."
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}