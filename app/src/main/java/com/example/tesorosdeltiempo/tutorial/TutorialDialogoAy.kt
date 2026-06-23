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

    private data class VistaTutorial(
        val titulo: TextView,
        val progreso: TextView,
        val videoView: VideoView,
        val sinVideo: TextView,
        val texto: TextView
    )

    fun mostrar(
        activity: AppCompatActivity,
        incluirPasosCuidador: Boolean,
        marcarComoVistoAlTerminar: Boolean = false
    ) {
        val pasos = construirPasos(incluirPasosCuidador)
        val vista = LayoutInflater.from(activity).inflate(R.layout.dialog_tutorial_paso, null)
        val refs = crearVistaTutorial(vista)

        val dialogo = AlertDialog.Builder(activity)
            .setView(vista)
            .setCancelable(true)
            .setPositiveButton(R.string.tutorial_siguiente, null)
            .setNegativeButton(R.string.tutorial_anterior, null)
            .setNeutralButton(R.string.tutorial_cerrar, null)
            .create()

        dialogo.setOnDismissListener { detenerVideo(refs.videoView) }
        dialogo.setOnShowListener {
            configurarVentana(activity, dialogo)
            enlazarBotones(dialogo, activity, pasos, refs, marcarComoVistoAlTerminar)
        }

        dialogo.show()
    }

    private fun construirPasos(incluirPasosCuidador: Boolean): List<Paso> {
        val pasos = pasosBase().toMutableList()
        if (incluirPasosCuidador) {
            pasos.addAll(pasosCuidador())
        }
        return pasos
    }

    private fun crearVistaTutorial(vista: View) = VistaTutorial(
        titulo = vista.findViewById(R.id.tvTutorialTitulo),
        progreso = vista.findViewById(R.id.tvTutorialProgreso),
        videoView = vista.findViewById(R.id.videoTutorial),
        sinVideo = vista.findViewById(R.id.tvTutorialSinVideo),
        texto = vista.findViewById(R.id.tvTutorialTexto)
    )

    private fun configurarVentana(activity: AppCompatActivity, dialogo: AlertDialog) {
        val ancho = (activity.resources.displayMetrics.widthPixels * 0.92).toInt()
        dialogo.window?.setLayout(ancho, android.view.WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun detenerVideo(videoView: VideoView) {
        try {
            videoView.stopPlayback()
        } catch (_: Exception) {
            // Se ignora por si el vídeo puede no estar inicializado o ya estar detenido
        }
    }

    private fun cargarPaso(
        activity: AppCompatActivity,
        pasos: List<Paso>,
        refs: VistaTutorial,
        indice: Int
    ) {
        val paso = pasos[indice]
        refs.titulo.setText(paso.tituloRes)
        refs.texto.setText(paso.textoRes)
        refs.progreso.text = activity.getString(R.string.tutorial_progreso, indice + 1, pasos.size)

        // Usamos el ID del vídeo
        if (paso.videoRawRes != null && paso.videoRawRes != 0) {
            mostrarVideo(activity, refs, paso.videoRawRes)
        } else {
            ocultarVideo(refs)
        }
    }

    private fun mostrarVideo(activity: AppCompatActivity, refs: VistaTutorial, videoRawRes: Int) {
        refs.sinVideo.visibility = View.GONE
        (refs.videoView.parent as? ViewGroup)?.visibility = View.VISIBLE
        detenerVideo(refs.videoView)

        refs.videoView.setVideoURI(
            Uri.parse("android.resource://${activity.packageName}/$videoRawRes")
        )
        refs.videoView.setOnErrorListener { _, _, _ ->
            ocultarVideo(refs)
            true
        }
        refs.videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            aplicarEscaladoVideo(mp)
            mp.start()
        }
    }

    private fun aplicarEscaladoVideo(mp: android.media.MediaPlayer) {
        try {
            mp.setVideoScalingMode(
                android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            )
        } catch (_: Exception) {
            // Se ignora ya que algunos dispositivos no soportan el modo de escalado
        }
    }
    private fun ocultarVideo(refs: VistaTutorial) {
        detenerVideo(refs.videoView)
        (refs.videoView.parent as? ViewGroup)?.visibility = View.GONE
        refs.sinVideo.visibility = View.VISIBLE
    }

    private fun actualizarBotones(
        dialogo: AlertDialog,
        activity: AppCompatActivity,
        pasos: List<Paso>,
        indice: Int
    ) {
        val btnPos = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNeg = dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
        btnNeg.isEnabled = indice > 0
        btnNeg.alpha = if (indice > 0) 1f else 0.4f
        btnPos.text = if (indice < pasos.size - 1) {
            activity.getString(R.string.tutorial_siguiente)
        } else {
            activity.getString(R.string.tutorial_finalizar)
        }
    }

    private fun enlazarBotones(
        dialogo: AlertDialog,
        activity: AppCompatActivity,
        pasos: List<Paso>,
        refs: VistaTutorial,
        marcarComoVistoAlTerminar: Boolean
    ) {
        var indice = 0
        cargarPaso(activity, pasos, refs, indice)
        actualizarBotones(dialogo, activity, pasos, indice)

        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            if (indice > 0) {
                indice--
                cargarPaso(activity, pasos, refs, indice)
                actualizarBotones(dialogo, activity, pasos, indice)
            }
        }

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (indice < pasos.size - 1) {
                indice++
                cargarPaso(activity, pasos, refs, indice)
                actualizarBotones(dialogo, activity, pasos, indice)
            } else {
                marcarVistoSiCorresponde(activity, marcarComoVistoAlTerminar)
                dialogo.dismiss()
            }
        }

        dialogo.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            marcarVistoSiCorresponde(activity, marcarComoVistoAlTerminar)
            dialogo.dismiss()
        }
    }

    private fun marcarVistoSiCorresponde(activity: AppCompatActivity, marcar: Boolean) {
        if (marcar) {
            TutorialPreferencias.marcarComoVisto(activity)
        }
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