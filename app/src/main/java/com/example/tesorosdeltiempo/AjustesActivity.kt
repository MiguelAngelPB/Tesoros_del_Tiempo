package com.example.tesorosdeltiempo

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tesorosdeltiempo.seguridad.CuidadorAuthManager
import com.example.tesorosdeltiempo.ui.BarraArribaAy
import android.text.Editable
import android.text.TextWatcher

// Ajustes login/registro cuidador, papelera (solo con sesión) y barra
class AjustesActivity : AppCompatActivity() {

    private lateinit var auth: CuidadorAuthManager
    private lateinit var tvEstadoCuidador: TextView
    private lateinit var btnLoginCuidador: Button
    private lateinit var btnRegistroCuidador: Button
    private lateinit var btnLogoutCuidador: Button
    private lateinit var btnPapelera: Button
    private lateinit var btnEliminarUsuarioCuidador: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)
        // Barra superior fija
        BarraArribaAy.ponerBarraArriba(this)

        // Gestión de credenciales cifradas del cuidador
        auth = CuidadorAuthManager(this)
        tvEstadoCuidador = findViewById(R.id.tvEstadoCuidador)
        btnLoginCuidador = findViewById(R.id.btnLoginCuidador)
        btnRegistroCuidador = findViewById(R.id.btnRegistroCuidador)
        btnLogoutCuidador = findViewById(R.id.btnLogoutCuidador)
        btnPapelera = findViewById(R.id.btnPapelera)
        btnEliminarUsuarioCuidador = findViewById(R.id.btnEliminarUsuarioCuidador)

        // Papelera solo con el cuidador conectado (se oculta si no)
        btnPapelera.setOnClickListener {
            startActivity(Intent(this, PapeleraActivity::class.java))
        }

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

        btnEliminarUsuarioCuidador.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar usuario cuidador")
                .setMessage("Se borrarán las credenciales guardadas de este dispositivo. ¿Continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar") { _, _ ->
                    auth.eliminarUsuarioCuidador()
                    actualizarEstadoUI()
                    Toast.makeText(this, "Usuario cuidador eliminado", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        actualizarEstadoUI()
    }

    // Al volver a la pantalla, refresco botones y texto
    override fun onResume() {
        super.onResume()
        actualizarEstadoUI()
    }

    // Texto de estado y qué botones se muestran según la sesión
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

        btnLogoutCuidador.visibility = if (loggedIn) View.VISIBLE else View.GONE
        btnEliminarUsuarioCuidador.visibility = if (loggedIn) View.VISIBLE else View.GONE
        btnPapelera.visibility = if (loggedIn) View.VISIBLE else View.GONE
    }

    private enum class ModoAuth { LOGIN, REGISTRO }

    // Diálogo login o registro con usuario y contraseña
    private fun mostrarDialogoAuth(modo: ModoAuth) {
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }
        
        val etUsername = EditText(this).apply {
            hint = "Usuario (cuidador)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            isSingleLine = true
            maxLines = 1
        }
        val etPassword = EditText(this).apply {
            hint = "Contraseña"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
            maxLines = 1
        }

        // Quitamos espacios y saltos en cuanto se escriban
        val limpiaEspacios = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val original = s?.toString().orEmpty()
                val limpio = original.filterNot { it.isWhitespace() }
                if (original != limpio) s?.replace(0, s.length, limpio)
            }
        }
        etUsername.addTextChangedListener(limpiaEspacios)
        etPassword.addTextChangedListener(limpiaEspacios)

        contenedor.addView(etUsername)
        contenedor.addView(etPassword)

        val titulo = when (modo) {
            ModoAuth.LOGIN -> "Iniciar sesión"
            ModoAuth.REGISTRO -> "Registrarse"
        }

        val dialogo = AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(contenedor)
            .setPositiveButton(if (modo == ModoAuth.LOGIN) "Entrar" else "Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val user = etUsername.text?.toString().orEmpty()
                val pass = etPassword.text?.toString().orEmpty()

                if (user.any { it.isWhitespace() } || pass.any { it.isWhitespace() }) {
                    Toast.makeText(
                        this,
                        "Usuario y contraseña no pueden tener espacios.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (!auth.usuarioValido(user)) {
                    Toast.makeText(
                        this,
                        "El usuario debe incluir @ y no tener espacios.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (modo == ModoAuth.REGISTRO && !auth.contrasenaSegura(pass)) {
                    Toast.makeText(
                        this,
                        "Contraseña: mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val ok = if (modo == ModoAuth.LOGIN) {
                    auth.iniciarSesion(user, pass)
                } else {
                    auth.registrar(user, pass)
                }

                if (ok) {
                    Toast.makeText(this, "¡Listo!", Toast.LENGTH_SHORT).show()
                    actualizarEstadoUI()
                    dialogo.dismiss()
                } else {
                    val msg = if (modo == ModoAuth.LOGIN) {
                        "Usuario o contraseña incorrectos (sin espacios y usuario con @)."
                    } else {
                        "Ya existe un cuidador registrado o no cumples las reglas de seguridad."
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialogo.show()
    }
}