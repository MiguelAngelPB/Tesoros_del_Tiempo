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
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// Ajustes login/registro cuidador, papelera (solo con sesión) y barra
class AjustesActivity : AppCompatActivity() {

    private lateinit var auth: CuidadorAuthManager
    private lateinit var tvEstadoCuidador: TextView
    private lateinit var btnLoginCuidador: Button
    private lateinit var btnRegistroCuidador: Button
    private lateinit var btnLogoutCuidador: Button
    private lateinit var btnPapelera: Button
    private lateinit var btnEliminarUsuarioCuidador: Button
    private lateinit var btnExportarGaleria: Button
    private lateinit var btnImportarGaleria: Button

    private val lanzadorExportarZip =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    GaleriaExportImport.exportarZipPortatil(this@AjustesActivity, uri).fold(
                        onSuccess = {
                            Toast.makeText(
                                this@AjustesActivity,
                                "Copia lista (sirve en otro móvil con esta app)",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(
                                this@AjustesActivity,
                                e.message ?: "Error al exportar",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }

    private val lanzadorImportarZip =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    GaleriaExportImport.importarZipPortatil(this@AjustesActivity, uri).fold(
                        onSuccess = { n ->
                            Toast.makeText(
                                this@AjustesActivity,
                                "Importados $n recuerdos (añadidos a los actuales)",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(
                                this@AjustesActivity,
                                e.message ?: "Error al importar",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }

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
        btnExportarGaleria = findViewById(R.id.btnExportarGaleria)
        btnImportarGaleria = findViewById(R.id.btnImportarGaleria)

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

        btnExportarGaleria.setOnClickListener {
            lanzadorExportarZip.launch(GaleriaExportImport.nombreCopiaSugerido())
        }

        btnImportarGaleria.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Importar en este móvil")
                .setMessage(
                    "Elige un .zip creado con «Exportar datos». " +
                            "Los recuerdos se añaden a los que ya hay. " +
                            "El archivo contiene las fotos/vídeos sin el cifrado de la app: guárdalo en un sitio seguro."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Elegir archivo") { _, _ ->
                    lanzadorImportarZip.launch(arrayOf("application/zip", "application/x-zip-compressed"))
                }
                .show()
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
        btnExportarGaleria.visibility = if (loggedIn) View.VISIBLE else View.GONE
        btnImportarGaleria.visibility = if (loggedIn) View.VISIBLE else View.GONE
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

        val etRecuerdoBonito = EditText(this).apply {
            hint = "Recuerdo más bonito con el paciente (8–20 caracteres)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            isSingleLine = true
            maxLines = 1
        }

        val avisoRecuerdo = TextView(this).apply {
            text =
                "Copia y pega o escribe esta frase en un lugar seguro, es muy importante."
            setPadding(0, 8, 0, 0)
            textSize = 13f
            setTextColor(0xFF555555.toInt())
        }

        val tvOlvide = TextView(this).apply {
            text = "Olvidé mi contraseña"
            setPadding(0, 16, 0, 0)
            textSize = 15f
            setTextColor(0xFF1565C0.toInt())
            isClickable = true
            isFocusable = true
        }

        val sinEspaciosNiSaltos = watcherQuitaTodoBlanco()
        etUsername.addTextChangedListener(sinEspaciosNiSaltos)
        etPassword.addTextChangedListener(sinEspaciosNiSaltos)
        etRecuerdoBonito.addTextChangedListener(watcherSoloQuitaSaltosLinea())

        contenedor.addView(etUsername)
        contenedor.addView(etPassword)
        if (modo == ModoAuth.REGISTRO) {
            contenedor.addView(etRecuerdoBonito)
            contenedor.addView(avisoRecuerdo)
        } else {
            contenedor.addView(tvOlvide)
        }

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
                if (modo == ModoAuth.REGISTRO) {
                    val recuerdo = etRecuerdoBonito.text?.toString().orEmpty()
                    if (!auth.recuerdoBonitoValido(recuerdo)) {
                        Toast.makeText(
                            this,
                            "El recuerdo debe tener entre 8 y 20 caracteres.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                }

                val ok = if (modo == ModoAuth.LOGIN) {
                    auth.iniciarSesion(user, pass)
                } else {
                    auth.registrar(user, pass, etRecuerdoBonito.text?.toString().orEmpty())
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

        if (modo == ModoAuth.LOGIN) {
            tvOlvide.setOnClickListener {
                mostrarDialogoRecuperacionFrase(dialogo)
            }
        }
        dialogo.show()
    }

    private fun mostrarDialogoRecuperacionFrase(dialogoLogin: AlertDialog) {
        if (!auth.hayCuidadorRegistrado()) {
            Toast.makeText(this, "No hay ningún cuidador registrado en este dispositivo.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!auth.tieneFraseRecuperacionGuardada()) {
            Toast.makeText(
                this,
                "Esta cuenta no tiene frase de recuperación. Inicia sesión con tu contraseña o elimina el usuario y vuelve a registrarte.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val etFrase = EditText(this).apply {
            hint = "Tu recuerdo más bonito con el paciente"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            isSingleLine = true
            maxLines = 1
            addTextChangedListener(watcherSoloQuitaSaltosLinea())
        }

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 8, 48, 0)
            addView(etFrase)
        }

        val dlg = AlertDialog.Builder(this)
            .setTitle("Recuperar contraseña")
            .setView(contenedor)
            .setPositiveButton("Siguiente", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val frase = etFrase.text?.toString().orEmpty()
                if (!auth.recuerdoBonitoValido(frase)) {
                    Toast.makeText(
                        this,
                        "El recuerdo debe tener entre 8 y 20 caracteres",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                if (!auth.verificarFraseRecuperacion(frase)) {
                    Toast.makeText(this, "La frase no coincide con la guardada al registrarte.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                dlg.dismiss()
                mostrarDialogoNuevaContrasena(frase, dialogoLogin)
            }
        }
        dlg.show()
    }

    // Quitamos los espacios y los saltos de línea del usuario y la contraseña
    private fun watcherQuitaTodoBlanco(): TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            val original = s?.toString().orEmpty()
            val limpio = original.filterNot { it.isWhitespace() }
            if (original != limpio) s?.replace(0, s.length, limpio)
        }
    }

    // Quitamos los saltos de linea
    private fun watcherSoloQuitaSaltosLinea(): TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            val original = s?.toString().orEmpty()
            val sinSaltos = original.replace("\r", "").replace("\n", "")
            if (original != sinSaltos) s?.replace(0, s.length, sinSaltos)
        }
    }

    private fun mostrarDialogoNuevaContrasena(fraseVerificada: String, dialogoLogin: AlertDialog) {
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }
        val etNueva = EditText(this).apply {
            hint = "Nueva contraseña"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
            maxLines = 1
        }
        val etRepite = EditText(this).apply {
            hint = "Repite la nueva contraseña"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
            maxLines = 1
        }
        val sinEspaciosNiSaltos = watcherQuitaTodoBlanco()
        etNueva.addTextChangedListener(sinEspaciosNiSaltos)
        etRepite.addTextChangedListener(sinEspaciosNiSaltos)
        contenedor.addView(etNueva)
        contenedor.addView(etRepite)

        val dlg = AlertDialog.Builder(this)
            .setTitle("Nueva contraseña")
            .setView(contenedor)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = etNueva.text?.toString().orEmpty()
                val r = etRepite.text?.toString().orEmpty()
                if (n != r) {
                    Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!auth.contrasenaSegura(n)) {
                    Toast.makeText(
                        this,
                        "Contraseña: mínimo 8 caracteres, 1 mayúscula, 1 número y 1 símbolo.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                if (auth.restablecerContrasenaConFrase(fraseVerificada, n)) {
                    Toast.makeText(this, "Contraseña actualizada. Has iniciado sesión.", Toast.LENGTH_SHORT).show()
                    dialogoLogin.dismiss()
                    dlg.dismiss()
                    actualizarEstadoUI()
                } else {
                    Toast.makeText(this, "No se pudo guardar la contraseña.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dlg.show()
    }
}