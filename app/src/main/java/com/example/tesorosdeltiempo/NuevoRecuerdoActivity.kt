package com.example.tesorosdeltiempo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.ui.RecuerdosViewModel
import com.example.tesorosdeltiempo.ui.RecuerdosViewModelFactory
import com.example.tesorosdeltiempo.ui.BarraArribaAy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NuevoRecuerdoActivity : AppCompatActivity() {

    private val viewModel: RecuerdosViewModel by viewModels {
        RecuerdosViewModelFactory(this)
    }

    private lateinit var etTitulo: EditText
    private lateinit var etTags: EditText
    private lateinit var etDescripcionTexto: EditText
    private lateinit var tvResumenAdjuntos: TextView

    private var pendingPhotoPath: String? = null
    private var selectedMainPath: String? = null
    private var selectedMainType: String? = null
    private var selectedDescriptionPath: String? = null
    private var selectedDescriptionType: String? = null

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val path = pendingPhotoPath
            pendingPhotoPath = null
            if (success && !path.isNullOrEmpty()) {
                selectedMainPath = path
                selectedMainType = "FOTO"
                actualizarResumen()
            }
        }

    private val videoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                lifecycleScope.launch {
                    selectedMainPath = copiarUriAInterno(uri, "video")
                    selectedMainType = "VIDEO"
                    actualizarResumen()
                }
            }
        }

    private val audioLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                lifecycleScope.launch {
                    selectedMainPath = copiarUriAInterno(uri, "audio")
                    selectedMainType = "AUDIO"
                    actualizarResumen()
                }
            }
        }

    private val archivoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val path = copiarUriAInterno(uri, "archivo")
                    val mimeType = contentResolver.getType(uri).orEmpty()
                    val tipo = when {
                        mimeType.startsWith("image") -> "FOTO"
                        mimeType.startsWith("video") -> "VIDEO"
                        mimeType.startsWith("audio") -> "AUDIO"
                        else -> "TEXTO"
                    }
                    selectedMainPath = path
                    selectedMainType = tipo
                    actualizarResumen()
                }
            }
        }

    private val descripcionMultimediaLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val path = copiarUriAInterno(uri, "desc")
                    val mimeType = contentResolver.getType(uri).orEmpty()
                    val tipo = when {
                        mimeType.startsWith("image") -> "FOTO"
                        mimeType.startsWith("video") -> "VIDEO"
                        mimeType.startsWith("audio") -> "AUDIO"
                        else -> null
                    }
                    selectedDescriptionPath = path
                    selectedDescriptionType = tipo
                    actualizarResumen()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_recuerdo)
        // Barra de arriba fija
        BarraArribaAy.ponerBarraArriba(this)

        etTitulo = findViewById(R.id.etTituloRecuerdo)
        etTags = findViewById(R.id.etTagsRecuerdo)
        etDescripcionTexto = findViewById(R.id.etDescripcionTextoOpcional)
        tvResumenAdjuntos = findViewById(R.id.tvResumenAdjuntos)

        findViewById<Button>(R.id.btnFotoNuevo).setOnClickListener { hacerFoto() }
        findViewById<Button>(R.id.btnVideoNuevo).setOnClickListener { grabarVideo() }
        findViewById<Button>(R.id.btnAudioNuevo).setOnClickListener { grabarAudio() }
        findViewById<Button>(R.id.btnArchivoNuevo).setOnClickListener {
            archivoLauncher.launch(arrayOf("image/*", "video/*", "audio/*"))
        }
        findViewById<Button>(R.id.btnAdjuntarDescripcionMultimedia).setOnClickListener {
            descripcionMultimediaLauncher.launch(arrayOf("image/*", "video/*", "audio/*"))
        }
        findViewById<Button>(R.id.btnTextoSoloNuevo).setOnClickListener {
            usarTextoComoPrincipal()
        }
        findViewById<Button>(R.id.btnGuardarRecuerdo).setOnClickListener {
            guardarRecuerdo()
        }
    }

    private fun hacerFoto() {
        val photoFile = File(filesDir, "foto_${System.currentTimeMillis()}.jpg")
        pendingPhotoPath = photoFile.absolutePath
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(uri)
    }

    private fun grabarVideo() {
        videoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
    }

    private fun grabarAudio() {
        audioLauncher.launch(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION))
    }

    private fun usarTextoComoPrincipal() {
        selectedMainType = "TEXTO"
        selectedMainPath = ""
        actualizarResumen()
    }

    private fun actualizarResumen() {
        val principal = if (!selectedMainType.isNullOrEmpty()) {
            "Principal: $selectedMainType"
        } else {
            "Principal: sin seleccionar"
        }
        val descripcion = if (!selectedDescriptionType.isNullOrEmpty()) {
            "Descripción multimedia: $selectedDescriptionType"
        } else {
            "Descripción multimedia: no adjunta"
        }
        tvResumenAdjuntos.text = "$principal\n$descripcion"
    }

    private fun guardarRecuerdo() {
        val tipoPrincipal = selectedMainType
        if (tipoPrincipal.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Selecciona primero el contenido principal del recuerdo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val recuerdo = construirRecuerdo(
            tipoPrincipal,
            selectedMainPath.orEmpty()
        )
        viewModel.guardarRecuerdo(recuerdo)
        Toast.makeText(this, "Recuerdo guardado correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun construirRecuerdo(tipoPrincipal: String, rutaPrincipal: String): RecuerdosEntity {
        val titulo = etTitulo.text?.toString()?.trim().orEmpty()
        val tags = etTags.text?.toString()?.trim().orEmpty()
        val textoDesc = etDescripcionTexto.text?.toString()?.trim().orEmpty()


        return RecuerdosEntity(
            title = if (titulo.isBlank()) generarTituloAutomatico(tipoPrincipal) else titulo,
            filePath = rutaPrincipal,
            type = tipoPrincipal,
            tags = tags,
            // Description de texto
            description = textoDesc.ifBlank { null },
            // Esto es solo para el archivo adjunto de la descripción si existe
            descriptionType = selectedDescriptionType,
            // Texto asociado a la descripciónsi existe
            descriptionContent = textoDesc.ifBlank { null },
            descriptionPath = selectedDescriptionPath
        )
    }

    private suspend fun copiarUriAInterno(uri: Uri, prefijo: String): String =
        withContext(Dispatchers.IO) {
            val extension = when {
                contentResolver.getType(uri)?.startsWith("image") == true -> ".jpg"
                contentResolver.getType(uri)?.startsWith("video") == true -> ".mp4"
                contentResolver.getType(uri)?.startsWith("audio") == true -> ".m4a"
                else -> ""
            }
            val nombre = "${prefijo}_${System.currentTimeMillis()}$extension"
            val destino = File(filesDir, nombre)
            val input: InputStream? = contentResolver.openInputStream(uri)
            input.use { i ->
                FileOutputStream(destino).use { o ->
                    if (i != null) i.copyTo(o)
                }
            }
            destino.absolutePath
        }

    private fun generarTituloAutomatico(tipo: String): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return "$tipo - ${formato.format(Date())}"
    }
}