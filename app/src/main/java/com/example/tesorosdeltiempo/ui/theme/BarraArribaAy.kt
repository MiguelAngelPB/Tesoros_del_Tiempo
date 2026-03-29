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
    }
}