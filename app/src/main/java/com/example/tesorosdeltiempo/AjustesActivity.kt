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
import com.example.tesorosdeltiempo.tutorial.TutorialDialogoAy

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
    private lateinit var btnCambiarIdioma: Button
    private lateinit var btnTutorial: Button

    private val lanzadorExportarZip =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    GaleriaExportImport.exportarZipPortatil(this@AjustesActivity, uri).fold(
                        onSuccess = {
                            Toast.makeText(
                                this@AjustesActivity,
                                getString(R.string.copia_lista_sirve_en_otro_m_vil_con_esta_app),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(
                                this@AjustesActivity,
                                e.message ?: getString(R.string.error_al_exportar),
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
                                getString(R.string.importados_recuerdos_a_adidos_a_los_actuales, n),
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(
                                this@AjustesActivity,
                                e.message ?: getString(R.string.error_al_importar),
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
        btnCambiarIdioma = findViewById(R.id.btnCambiarIdioma)
        btnTutorial = findViewById(R.id.btnTutorial)

        btnTutorial.setOnClickListener {
            TutorialDialogoAy.mostrar(
                activity = this,
                incluirPasosCuidador = auth.estaConectado(),
                marcarComoVistoAlTerminar = false
            )
        }

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
            Toast.makeText(this, getString(R.string.sesi_n_cerrada), Toast.LENGTH_SHORT).show()
        }

        btnExportarGaleria.setOnClickListener {
            lanzadorExportarZip.launch(GaleriaExportImport.nombreCopiaSugerido())
        }

        btnImportarGaleria.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.importar_en_este_m_vil))
                .setMessage(getString(R.string.mensaje_importar_galeria))
                .setNegativeButton(getString(R.string.cancelar), null)
                .setPositiveButton(getString(R.string.elegir_archivo)) { _, _ ->
                    lanzadorImportarZip.launch(arrayOf("application/zip", "application/x-zip-compressed"))
                }
                .show()
        }

        btnCambiarIdioma.setOnClickListener {
            mostrarDialogoIdioma()
        }

        btnEliminarUsuarioCuidador.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.eliminar_usuario_cuidador))
                .setMessage(getString(R.string.se_borrar_n_las_credenciales_guardadas_de_este_dispositivo_continuar))
                .setNegativeButton(getString(R.string.cancelar), null)
                .setPositiveButton(getString(R.string.eliminar)) { _, _ ->
                    auth.eliminarUsuarioCuidador()
                    actualizarEstadoUI()
                    Toast.makeText(this,
                        getString(R.string.usuario_cuidador_eliminado), Toast.LENGTH_SHORT).show()
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
            getString(R.string.cuidador_conectado, user)
        } else {
            getString(R.string.no_has_iniciado_sesi_n_como_cuidador)
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
        btnCambiarIdioma.visibility = if (loggedIn) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogoIdioma() {
        // Textos que verá el usuario en el diálogo
        val idiomas = arrayOf("Español", "English")
        // Códigos de los idiomas
        val codigos = arrayOf("es", "en")

        // Averiguar qué idioma está puesto ahora mismo para dejarlo marcado
        val idiomaActual = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val indiceSeleccionado = if (idiomaActual.contains("en")) 1 else 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.selecciona_idioma))
            .setSingleChoiceItems(idiomas, indiceSeleccionado) { dialog, which ->
                val codigoSeleccionado = codigos[which]

                // Se aplica el idioma y se reinicia la pantalla
                val locales = androidx.core.os.LocaleListCompat.forLanguageTags(codigoSeleccionado)
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales)

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private enum class ModoAuth { LOGIN, REGISTRO }

    // Diálogo login o registro con usuario y contraseña
    private fun mostrarDialogoAuth(modo: ModoAuth) {
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 0)
        }
        val etUsername = EditText(this).apply {
            hint = context.getString(R.string.usuario_cuidador)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            isSingleLine = true
            maxLines = 1
        }
        val etPassword = EditText(this).apply {
            hint = context.getString(R.string.contrase_a)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
            maxLines = 1
        }

        val etRecuerdoBonito = EditText(this).apply {
            hint = context.getString(R.string.recuerdo_m_s_bonito_con_el_paciente_8_20_caracteres)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            isSingleLine = true
            maxLines = 1
        }

        val avisoRecuerdo = TextView(this).apply {
            text =
                context.getString(R.string.copia_y_pega_o_escribe_esta_frase_en_un_lugar_seguro_es_muy_importante)
            setPadding(0, 8, 0, 0)
            textSize = 13f
            setTextColor(0xFF555555.toInt())
        }

        val tvOlvide = TextView(this).apply {
            text = context.getString(R.string.olvid_mi_contrase_a)
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
            ModoAuth.LOGIN -> getString(R.string.iniciar_sesi_n)
            ModoAuth.REGISTRO -> getString(R.string.registrarse)
        }

        val dialogo = AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(contenedor)
            .setPositiveButton(if (modo == ModoAuth.LOGIN) getString(R.string.entrar) else getString(
                R.string.guardar
            ), null)
            .setNegativeButton(getString(R.string.cancelar), null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val user = etUsername.text?.toString().orEmpty()
                val pass = etPassword.text?.toString().orEmpty()

                if (user.any { it.isWhitespace() } || pass.any { it.isWhitespace() }) {
                    Toast.makeText(
                        this,
                        getString(R.string.usuario_y_contrase_a_no_pueden_tener_espacios),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (!auth.usuarioValido(user)) {
                    Toast.makeText(
                        this,
                        getString(R.string.el_usuario_debe_incluir_y_no_tener_espacios),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (modo == ModoAuth.REGISTRO && !auth.contrasenaSegura(pass)) {
                    Toast.makeText(
                        this,
                        getString(R.string.contrase_a_m_nimo_8_caracteres_1_may_scula_1_n_mero_y_1_s_mbolo),
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                if (modo == ModoAuth.REGISTRO) {
                    val recuerdo = etRecuerdoBonito.text?.toString().orEmpty()
                    if (!auth.recuerdoBonitoValido(recuerdo)) {
                        Toast.makeText(
                            this,
                            getString(R.string.el_recuerdo_debe_tener_entre_8_y_20_caracteres),
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
                    Toast.makeText(this, getString(R.string.listo), Toast.LENGTH_SHORT).show()
                    actualizarEstadoUI()
                    dialogo.dismiss()
                } else {
                    val msg = if (modo == ModoAuth.LOGIN) {
                        getString(R.string.usuario_o_contrase_a_incorrectos_sin_espacios_y_usuario_con)
                    } else {
                        getString(R.string.ya_existe_un_cuidador_registrado_o_no_cumples_las_reglas_de_seguridad)
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
            Toast.makeText(this,
                getString(R.string.no_hay_ning_n_cuidador_registrado_en_este_dispositivo), Toast.LENGTH_SHORT).show()
            return
        }
        if (!auth.tieneFraseRecuperacionGuardada()) {
            Toast.makeText(
                this,
                getString(R.string.esta_cuenta_no_tiene_frase_de_recuperaci_n_inicia_sesi_n_con_tu_contrase_a_o_elimina_el_usuario_y_vuelve_a_registrarte),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val etFrase = EditText(this).apply {
            hint = context.getString(R.string.tu_recuerdo_m_s_bonito_con_el_paciente)
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
            .setTitle(getString(R.string.recuperar_contrase_a))
            .setView(contenedor)
            .setPositiveButton(getString(R.string.siguiente), null)
            .setNegativeButton(getString(R.string.cancelar), null)
            .create()

        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val frase = etFrase.text?.toString().orEmpty()
                if (!auth.recuerdoBonitoValido(frase)) {
                    Toast.makeText(
                        this,
                        getString(R.string.el_recuerdo_debe_tener_entre_8_y_20_caracteres),
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                if (!auth.verificarFraseRecuperacion(frase)) {
                    Toast.makeText(this,
                        getString(R.string.la_frase_no_coincide_con_la_guardada_al_registrarte), Toast.LENGTH_LONG).show()
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
            hint = context.getString(R.string.nueva_contrase_a)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
            maxLines = 1
        }
        val etRepite = EditText(this).apply {
            hint = context.getString(R.string.repite_la_nueva_contrase_a)
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
            .setTitle(getString(R.string.nueva_contrase_a))
            .setView(contenedor)
            .setPositiveButton(getString(R.string.guardar), null)
            .setNegativeButton(getString(R.string.cancelar), null)
            .create()

        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = etNueva.text?.toString().orEmpty()
                val r = etRepite.text?.toString().orEmpty()
                if (n != r) {
                    Toast.makeText(this,
                        getString(R.string.las_contrase_as_no_coinciden), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!auth.contrasenaSegura(n)) {
                    Toast.makeText(
                        this,
                        getString(R.string.contrase_a_m_nimo_8_caracteres_1_may_scula_1_n_mero_y_1_s_mbolo),
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                if (auth.restablecerContrasenaConFrase(fraseVerificada, n)) {
                    Toast.makeText(this,
                        getString(R.string.contrase_a_actualizada_has_iniciado_sesi_n), Toast.LENGTH_SHORT).show()
                    dialogoLogin.dismiss()
                    dlg.dismiss()
                    actualizarEstadoUI()
                } else {
                    Toast.makeText(this,
                        getString(R.string.no_se_pudo_guardar_la_contrase_a), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dlg.show()
    }
}