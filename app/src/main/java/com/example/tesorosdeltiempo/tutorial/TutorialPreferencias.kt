package com.example.tesorosdeltiempo.tutorial

import android.content.Context

// Recuerda si el tutorial ya se mostró automáticamente la primera vez
object TutorialPreferencias {

    private const val PREFS = "tutorial_prefs"
    private const val CLAVE_VISTO = "tutorial_visto"

    fun yaSeMostro(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(CLAVE_VISTO, false)

    fun marcarComoVisto(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(CLAVE_VISTO, true)
            .apply()
    }
}