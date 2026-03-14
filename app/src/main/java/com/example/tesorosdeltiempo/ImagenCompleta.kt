package com.example.tesorosdeltiempo

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.MediaPlayer

class ImagenCompleta : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var audioButton: Button
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_imagen_completa)

        imageView = findViewById(R.id.iv_foto)
        videoView = findViewById(R.id.vv_video)
        audioButton = findViewById(R.id.btn_audio_play_pause)

        supportActionBar?.title = "Foto Completa"

        val filePath = intent.getStringExtra("filePath")
        val type = intent.getStringExtra("type") ?: "FOTO"

        when (type) {
            "FOTO" -> {
                audioButton.visibility = View.GONE
                videoView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                if (!filePath.isNullOrEmpty()) {
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            "VIDEO" -> {
                audioButton.visibility = View.GONE
                imageView.visibility = View.GONE
                videoView.visibility = View.VISIBLE

                if (!filePath.isNullOrEmpty()) {
                    val uri = Uri.fromFile(java.io.File(filePath))

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
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(android.R.drawable.ic_btn_speak_now)
                audioButton.visibility = View.VISIBLE
                audioButton.setOnClickListener {
                    if (filePath.isNullOrEmpty()) {
                        Toast.makeText(this, "Audio no disponible", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    toggleAudio(filePath)
                }
            }
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
    }
}
