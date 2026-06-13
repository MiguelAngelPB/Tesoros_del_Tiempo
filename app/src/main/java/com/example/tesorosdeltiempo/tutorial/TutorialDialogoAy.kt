package com.example.tesorosdeltiempo.tutorial

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tesorosdeltiempo.R

// Tutorial en ventanas emergentes con vídeo corto + texto.
object TutorialDialogoAy {

    private data class Paso(
        val tituloRes: Int,
        val textoRes: Int,
        val videoRawRes: Int?
    )

    fun mostrar(
        activity: AppCompatActivity,
        incluirPasosCuidador: Boolean,
        marcarComoVistoAlTerminar: Boolean = false
    ) {
        val pasos = pasosBase().toMutableList()
        if (incluirPasosCuidador) {
            pasos.addAll(pasosCuidador())
        }

        val vista = LayoutInflater.from(activity).inflate(R.layout.dialog_tutorial_paso, null)
        val tvTitulo = vista.findViewById<TextView>(R.id.tvTutorialTitulo)
        val tvProgreso = vista.findViewById<TextView>(R.id.tvTutorialProgreso)
        val videoView = vista.findViewById<VideoView>(R.id.videoTutorial)
        val tvSinVideo = vista.findViewById<TextView>(R.id.tvTutorialSinVideo)
        val tvTexto = vista.findViewById<TextView>(R.id.tvTutorialTexto)

        var indice = 0

        fun detenerVideo() {
            try {
                videoView.stopPlayback()
            } catch (_: Exception) { }
        }

        fun cargarPaso(i: Int) {
            val paso = pasos[i]
            tvTitulo.setText(paso.tituloRes)
            tvTexto.setText(paso.textoRes)
            tvProgreso.text = activity.getString(R.string.tutorial_progreso, i + 1, pasos.size)

            // Usamos el ID del vídeo
            if (paso.videoRawRes != null && paso.videoRawRes != 0) {
                tvSinVideo.visibility = View.GONE
                (videoView.parent as? ViewGroup)?.visibility = View.VISIBLE
                detenerVideo()

                // Pasamos el ID del vídeo a la URI
                videoView.setVideoURI(
                    Uri.parse("android.resource://${activity.packageName}/${paso.videoRawRes}")
                )

                // Si el vídeo falla en la APK, lo ocultamos y mostramos el texto
                videoView.setOnErrorListener { _, _, _ ->
                    detenerVideo()
                    (videoView.parent as? ViewGroup)?.visibility = View.GONE
                    tvSinVideo.visibility = View.VISIBLE
                    true
                }

                videoView.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    try {
                        mp.setVideoScalingMode(
                            android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                        )
                    } catch (_: Exception) { }
                    mp.start()
                }
            } else {
                detenerVideo()
                (videoView.parent as? ViewGroup)?.visibility = View.GONE
                tvSinVideo.visibility = View.VISIBLE
            }
        }

        fun actualizarBotones(dialogo: AlertDialog, idx: Int) {
            val btnPos = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnNeg = dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
            btnNeg.isEnabled = idx > 0
            btnNeg.alpha = if (idx > 0) 1f else 0.4f
            btnPos.text = if (idx < pasos.size - 1) {
                activity.getString(R.string.tutorial_siguiente)
            } else {
                activity.getString(R.string.tutorial_finalizar)
            }
        }

        val dialogo = AlertDialog.Builder(activity)
            .setView(vista)
            .setCancelable(true)
            .setPositiveButton(R.string.tutorial_siguiente, null)
            .setNegativeButton(R.string.tutorial_anterior, null)
            .setNeutralButton(R.string.tutorial_cerrar, null)
            .create()

        dialogo.setOnDismissListener { detenerVideo() }

        dialogo.setOnShowListener {
            val ancho = (activity.resources.displayMetrics.widthPixels * 0.92).toInt()
            dialogo.window?.setLayout(ancho, android.view.WindowManager.LayoutParams.WRAP_CONTENT)

            cargarPaso(indice)
            actualizarBotones(dialogo, indice)

            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                if (indice > 0) {
                    indice--
                    cargarPaso(indice)
                    actualizarBotones(dialogo, indice)
                }
            }

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (indice < pasos.size - 1) {
                    indice++
                    cargarPaso(indice)
                    actualizarBotones(dialogo, indice)
                } else {
                    if (marcarComoVistoAlTerminar) {
                        TutorialPreferencias.marcarComoVisto(activity)
                    }
                    dialogo.dismiss()
                }
            }

            dialogo.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                if (marcarComoVistoAlTerminar) {
                    TutorialPreferencias.marcarComoVisto(activity)
                }
                dialogo.dismiss()
            }
        }

        dialogo.show()
    }

    private fun pasosBase(): List<Paso> = listOf(
        Paso(
            R.string.tutorial_paso1_titulo,
            R.string.tutorial_paso1_texto,
            R.raw.tutorial_anadir_recuerdo
        ),
        Paso(
            R.string.tutorial_paso2_titulo,
            R.string.tutorial_paso2_texto,
            R.raw.tutorial_buscar_recuerdo
        ),
        Paso(
            R.string.tutorial_paso3_titulo,
            R.string.tutorial_paso3_texto,
            R.raw.tutorial_ver_recuerdo
        ),
        Paso(
            R.string.tutorial_paso4_titulo,
            R.string.tutorial_paso4_texto,
            R.raw.tutorial_enviar_papelera
        )
    )

    private fun pasosCuidador(): List<Paso> = listOf(
        Paso(
            R.string.tutorial_paso5_titulo,
            R.string.tutorial_paso5_texto,
            R.raw.tutorial_restaurar_eliminar_papelera_cuidador
        ),
        Paso(
            R.string.tutorial_paso6_titulo,
            R.string.tutorial_paso6_texto,
            R.raw.tutorial_exportar_cuidador
        ),
        Paso(
            R.string.tutorial_paso7_titulo,
            R.string.tutorial_paso7_texto,
            R.raw.tutorial_importar_cuidador
        )
    )
}