package com.example.tesorosdeltiempo.ui

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tesorosdeltiempo.AjustesActivity
import com.example.tesorosdeltiempo.MainActivity
import com.example.tesorosdeltiempo.R
import com.example.tesorosdeltiempo.seguridad.CuidadorAuthManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.ViewGroup
import android.widget.Button
import android.view.LayoutInflater

/**
 Esto es un "helper" para no repetir lo mismo en todas las Activities
 */
object BarraArribaAy {

    fun ponerBarraArriba(
        activity: Activity,
        cuandoCambiaBusqueda: ((String) -> Unit)? = null
    ) {
        val botonInicio = activity.findViewById<ImageButton>(R.id.btnHomeTop)
        val botonAjustes = activity.findViewById<ImageButton>(R.id.btnSettingsTop)
        val buscador = activity.findViewById<EditText>(R.id.etSearchTop)

        // Esto es para que la barra no se meta debajo de la barra de notificaciones
        val barra = activity.findViewById<View>(R.id.barraArriba)

        if (barra != null) {
            ViewCompat.setOnApplyWindowInsetsListener(barra) { v, insets ->
                val barrasSistema = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Le añadimos el padding de arriba que necesite según el móvil.
                v.setPadding(v.paddingLeft, barrasSistema.top, v.paddingRight, v.paddingBottom)
                insets
            }
        }

        botonInicio.setOnClickListener {
            if (activity !is MainActivity) {
                val intent = Intent(activity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                activity.startActivity(intent)
            }
        }

        botonAjustes.setOnClickListener {
            if (activity !is AjustesActivity) {
                activity.startActivity(Intent(activity, AjustesActivity::class.java))
            }
        }

        // Buscador: cada vez que escribimos, se filtra por etiquetas
        buscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cuandoCambiaBusqueda?.invoke(s?.toString().orEmpty())
            }
        })

        ponerIndicadorCuidador(activity)
    }

    private fun ponerIndicadorCuidador(activity: Activity) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        val root = content.getChildAt(0) as? ConstraintLayout ?: return
        var indicador = root.findViewById<TextView>(R.id.tvIndicadorCuidador)
        if (indicador == null) {
            indicador = LayoutInflater.from(activity)
                .inflate(R.layout.indicador_cuidador, root, false) as TextView
            val margen = (12 * activity.resources.displayMetrics.density).toInt()
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                bottomMargin = margen
                marginStart = margen
            }
            root.addView(indicador, params)
            indicador.setOnClickListener { cerrarSesionCuidador(activity, indicador) }
        }

        actualizarIndicadorCuidador(activity, indicador)

        if (activity is LifecycleOwner) {
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    actualizarIndicadorCuidador(activity, indicador)
                }
            })
        }
    }

    private fun actualizarIndicadorCuidador(activity: Activity, indicador: TextView) {
        indicador.visibility = if (CuidadorAuthManager(activity).estaConectado()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun cerrarSesionCuidador(activity: Activity, indicador: TextView) {
        val btnLogout = activity.findViewById<Button?>(R.id.btnLogoutCuidador)
        if (btnLogout != null && btnLogout.visibility == View.VISIBLE) {
            btnLogout.performClick()
        } else {
            CuidadorAuthManager(activity).cerrarSesion()
            Toast.makeText(activity, activity.getString(R.string.sesi_n_cerrada), Toast.LENGTH_SHORT).show()
        }
        actualizarIndicadorCuidador(activity, indicador)
    }
}