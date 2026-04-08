package com.example.tesorosdeltiempo

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.MediaPlayer
import com.example.tesorosdeltiempo.ui.RecuerdosViewModel
import com.example.tesorosdeltiempo.ui.RecuerdosViewModelFactory
import com.example.tesorosdeltiempo.ui.BarraArribaAy
import com.example.tesorosdeltiempo.seguridad.AyArchivoSeguro
import android.view.LayoutInflater
import java.io.File

class ImagenCompleta : AppCompatActivity() {

    private val viewModel: RecuerdosViewModel by viewModels {
        RecuerdosViewModelFactory(this)
    }

    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var audioButton: Button
    private lateinit var textView: TextView
    private lateinit var btnBorrarRecuerdo: Button
    private lateinit var btnVerDatosRecuerdo: Button
    private var mediaPlayer: MediaPlayer? = null
    private var tempMainFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_imagen_completa)
        BarraArribaAy.ponerBarraArriba(this)

        imageView = findViewById(R.id.iv_foto)
        videoView = findViewById(R.id.vv_video)
        audioButton = findViewById(R.id.btn_audio_play_pause)
        textView = findViewById(R.id.tv_texto_recuerdo)
        btnBorrarRecuerdo = findViewById(R.id.btnBorrarRecuerdo)
        btnVerDatosRecuerdo = findViewById(R.id.btnVerDatosRecuerdo)

        supportActionBar?.title = "Foto Completa"

        val recuerdoId = intent.getLongExtra("id", -1L)
        val filePath = intent.getStringExtra("filePath")
        val type = intent.getStringExtra("type") ?: "FOTO"
        val title = intent.getStringExtra("title").orEmpty()
        val tags = intent.getStringExtra("tags").orEmpty()
        val descriptionType = intent.getStringExtra("descriptionType").orEmpty()
        val descriptionContent = intent.getStringExtra("descriptionContent").orEmpty()
        val descriptionPath = intent.getStringExtra("descriptionPath").orEmpty()

        when (type) {
            "FOTO" -> {
                audioButton.visibility = View.GONE
                videoView.visibility = View.GONE
                textView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                if (!filePath.isNullOrEmpty()) {
                    var tempFile: File? = null
                    try {
                        val rutaParaDecodificar = if (AyArchivoSeguro.esRutaArchivoCifrado(filePath)) {
                            tempFile = AyArchivoSeguro.descifrarAFicheroTemporal(this, filePath, "foto", ".jpg")
                            tempFile!!.absolutePath
                        } else {
                            filePath
                        }

                        val bitmap = BitmapFactory.decodeFile(rutaParaDecodificar)
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                        } else {
                            Toast.makeText(
                                this,
                                "No se pudo cargar la imagen",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (_: Exception) {
                        Toast.makeText(this, "No se pudo descifrar la imagen", Toast.LENGTH_SHORT)
                            .show()
                    } finally {
                        try { tempFile?.delete() } catch (_: Exception) { }
                    }
                }
            }
            "VIDEO" -> {
                audioButton.visibility = View.GONE
                textView.visibility = View.GONE
                imageView.visibility = View.GONE
                videoView.visibility = View.VISIBLE

                if (!filePath.isNullOrEmpty()) {
                    val rutaVideo = if (AyArchivoSeguro.esRutaArchivoCifrado(filePath)) {
                        tempMainFile = AyArchivoSeguro.descifrarAFicheroTemporal(this, filePath, "video", ".mp4")
                        tempMainFile!!.absolutePath
                    } else {
                        filePath
                    }

                    val uri = Uri.fromFile(File(rutaVideo))

                    val controller = MediaController(this)
                    controller.setAnchorView(videoView)
                    videoView.setMediaController(controller)

                    videoView.setVideoURI(uri)
                    videoView.setOnPreparedListener { mp ->
                        mp.isLooping = true
                        videoView.start()
                    }
                    videoView.setOnErrorListener { _, what, extra ->
                        Toast.makeText(
                            this,
                            "Error al reproducir el vídeo (what=$what, extra=$extra)",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                } else {
                    Toast.makeText(this, "Vídeo no disponible", Toast.LENGTH_SHORT).show()
                }
            }
            "AUDIO" -> {
                videoView.visibility = View.GONE
                textView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(android.R.drawable.ic_btn_speak_now)
                audioButton.visibility = View.VISIBLE
                audioButton.setOnClickListener {
                    if (filePath.isNullOrEmpty()) {
                        Toast.makeText(this, "Audio no disponible", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val rutaAudio = if (AyArchivoSeguro.esRutaArchivoCifrado(filePath)) {
                        if (tempMainFile == null) {
                            tempMainFile = AyArchivoSeguro.descifrarAFicheroTemporal(this, filePath, "audio", ".m4a")
                        }
                        tempMainFile!!.absolutePath
                    } else {
                        filePath
                    }
                    toggleAudio(rutaAudio)
                }
            }
            "TEXTO" -> {
                audioButton.visibility = View.GONE
                videoView.visibility = View.GONE
                imageView.visibility = View.GONE
                textView.visibility = View.VISIBLE
                val text = intent.getStringExtra("textContent")
                textView.text = if (text.isNullOrBlank()) {
                    "Recuerdo de texto sin contenido."
                } else {
                    text
                }
            }
        }

        btnVerDatosRecuerdo.setOnClickListener {
            val textoParaPersona = buildString {
                append(title.trim().ifBlank { "Sin título" })
                if (tags.isNotBlank()) {
                    append("\n\n")
                    append(tags.trim())
                }
                if (descriptionContent.isNotBlank()) {
                    append("\n\n")
                    append(descriptionContent.trim())
                }
            }

            val vista = LayoutInflater.from(this).inflate(R.layout.dialog_datos_recuerdo, null)
            val tvDatos = vista.findViewById<TextView>(R.id.tv_datos_recuerdo)
            val ivPreview = vista.findViewById<ImageView>(R.id.iv_preview_desc)
            val vvPreview = vista.findViewById<VideoView>(R.id.vv_preview_desc)
            val btnPlayPausa = vista.findViewById<Button>(R.id.btn_play_pausa_desc)

            tvDatos.text = textoParaPersona

            var tempDescFile: File? = null

            var reproductorAudioDesc: MediaPlayer? = null
            fun pararAudioDesc() {
                try {
                    reproductorAudioDesc?.release()
                } catch (_: Exception) { }
                reproductorAudioDesc = null
            }

            if (descriptionPath.isNotBlank()) {
                val pathParaTipo = if (AyArchivoSeguro.esRutaArchivoCifrado(descriptionPath)) {
                    descriptionPath.removeSuffix(".enc")
                } else {
                    descriptionPath
                }

                val tipoDescReal = when {
                    descriptionType.isNotBlank() -> descriptionType
                    pathParaTipo.endsWith(".jpg", true) || pathParaTipo.endsWith(".jpeg", true) || pathParaTipo.endsWith(".png", true) -> "FOTO"
                    pathParaTipo.endsWith(".mp4", true) || pathParaTipo.endsWith(".3gp", true) -> "VIDEO"
                    pathParaTipo.endsWith(".m4a", true) || pathParaTipo.endsWith(".mp3", true) || pathParaTipo.endsWith(".wav", true) -> "AUDIO"
                    else -> ""
                }

                val extTemp = when (tipoDescReal) {
                    "FOTO" -> ".jpg"
                    "VIDEO" -> ".mp4"
                    "AUDIO" -> ".m4a"
                    else -> ""
                }

                val descriptionPathReal = if (
                    AyArchivoSeguro.esRutaArchivoCifrado(descriptionPath) &&
                    extTemp.isNotBlank()
                ) {
                    tempDescFile = AyArchivoSeguro.descifrarAFicheroTemporal(
                        this,
                        descriptionPath,
                        "desc",
                        extTemp
                    )
                    tempDescFile!!.absolutePath
                } else {
                    descriptionPath
                }

                when (tipoDescReal) {
                    "FOTO" -> {
                        val bmp = BitmapFactory.decodeFile(descriptionPathReal)
                        if (bmp != null) {
                            ivPreview.visibility = View.VISIBLE
                            ivPreview.setImageBitmap(bmp)
                        }
                    }
                    "AUDIO" -> {
                        ivPreview.visibility = View.VISIBLE
                        ivPreview.setImageResource(android.R.drawable.ic_btn_speak_now)
                        btnPlayPausa.visibility = View.VISIBLE
                        btnPlayPausa.text = "Escuchar"

                        btnPlayPausa.setOnClickListener {
                            val mp = reproductorAudioDesc
                            if (mp == null) {
                                val nuevo = MediaPlayer()
                                nuevo.setDataSource(descriptionPathReal)
                                nuevo.setOnPreparedListener {
                                    btnPlayPausa.text = "Pausar"
                                    it.start()
                                }
                                nuevo.setOnCompletionListener {
                                    btnPlayPausa.text = "Escuchar"
                                    pararAudioDesc()
                                }
                                nuevo.prepareAsync()
                                reproductorAudioDesc = nuevo
                            } else {
                                if (mp.isPlaying) {
                                    mp.pause()
                                    btnPlayPausa.text = "Escuchar"
                                } else {
                                    mp.start()
                                    btnPlayPausa.text = "Pausar"
                                }
                            }
                        }
                    }
                    "VIDEO" -> {
                        vvPreview.visibility = View.VISIBLE

                        val uri = Uri.fromFile(java.io.File(descriptionPathReal))
                        val controller = MediaController(this)
                        controller.setAnchorView(vvPreview)
                        vvPreview.setMediaController(controller)
                        vvPreview.setVideoURI(uri)
                        vvPreview.setOnPreparedListener {
                            it.isLooping = true
                            vvPreview.start()
                        }

                        btnPlayPausa.visibility = View.VISIBLE
                        btnPlayPausa.text = "Pausar"
                        btnPlayPausa.setOnClickListener {
                            if (vvPreview.isPlaying) {
                                vvPreview.pause()
                                btnPlayPausa.text = "Ver vídeo"
                            } else {
                                vvPreview.start()
                                btnPlayPausa.text = "Pausar"
                            }
                        }
                    }
                }
            }

            val dialogo = AlertDialog.Builder(this)
                .setTitle("Te acuerdas…")
                .setView(vista)
                .setPositiveButton("Cerrar", null)
                .create()

            dialogo.setOnDismissListener {
                try { vvPreview.stopPlayback() } catch (_: Exception) { }
                pararAudioDesc()
                try { tempDescFile?.delete() } catch (_: Exception) { }
                tempDescFile = null
            }

            dialogo.show()
        }

        btnBorrarRecuerdo.setOnClickListener {
            if (recuerdoId <= 0L) {
                Toast.makeText(this, "No se pudo borrar este recuerdo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Borrar recuerdo")
                .setMessage("¿Seguro que quieres borrar este recuerdo?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Borrar") { _, _ ->
                    viewModel.borrarRecuerdoPorId(recuerdoId)
                    Toast.makeText(this, "Recuerdo borrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun toggleAudio(filePath: String) {
        val mp = mediaPlayer
        if (mp == null) {
            val nuevo = MediaPlayer()
            nuevo.setDataSource(filePath)
            nuevo.setOnPreparedListener {
                audioButton.text = "Pausar"
                it.start()
            }
            nuevo.setOnCompletionListener {
                audioButton.text = "Reproducir"
                it.reset()
                it.release()
                mediaPlayer = null
                try { tempMainFile?.delete() } catch (_: Exception) { }
                tempMainFile = null
            }
            nuevo.prepareAsync()
            mediaPlayer = nuevo
            return
        }

        if (mp.isPlaying) {
            mp.pause()
            audioButton.text = "Reproducir"
        } else {
            mp.start()
            audioButton.text = "Pausar"
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
        try { tempMainFile?.delete() } catch (_: Exception) { }
        tempMainFile = null
    }
}