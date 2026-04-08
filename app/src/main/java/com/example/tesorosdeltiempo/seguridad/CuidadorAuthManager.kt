package com.example.tesorosdeltiempo.seguridad

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Login/registro para el cuidador guardando usuario y contraseña cifrados
class CuidadorAuthManager(contexto: Context) {

    private val nombrePreferencias = "cuidador_secure_prefs"
    private val claveUsuario = "cuidador_username"
    private val claveContrasena = "cuidador_password"
    private val claveSesionIniciada = "cuidador_logged_in"

    private val preferenciasCifradas: SharedPreferences by lazy {
        crearPreferenciasCifradas(contexto)
    }

    private fun crearPreferenciasCifradas(contexto: Context): SharedPreferences {
        val claveMaestra = MasterKey.Builder(contexto)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Claves y valores cifrados
        return EncryptedSharedPreferences.create(
            contexto,
            nombrePreferencias,
            claveMaestra,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun estaConectado(): Boolean = preferenciasCifradas.getBoolean(claveSesionIniciada, false)

    fun obtenerUsuarioGuardado(): String? = preferenciasCifradas.getString(claveUsuario, null)

    // Un solo cuidador registrado
    fun registrar(nombreUsuario: String, contrasena: String): Boolean {
        val usuarioLimpio = nombreUsuario.trim()
        if (usuarioLimpio.isBlank() || contrasena.isBlank()) return false

        val usuarioYaGuardado = preferenciasCifradas.getString(claveUsuario, null)
        if (!usuarioYaGuardado.isNullOrBlank()) return false

        preferenciasCifradas.edit()
            .putString(claveUsuario, usuarioLimpio)
            .putString(claveContrasena, contrasena)
            .putBoolean(claveSesionIniciada, true)
            .apply()

        return true
    }

    // Comprueba usuario y contraseña con lo guardado
    fun iniciarSesion(nombreUsuario: String, contrasena: String): Boolean {
        val usuarioLimpio = nombreUsuario.trim()
        if (usuarioLimpio.isBlank() || contrasena.isBlank()) return false

        val usuarioGuardado = preferenciasCifradas.getString(claveUsuario, null) ?: return false
        val contrasenaGuardada = preferenciasCifradas.getString(claveContrasena, null) ?: return false

        return if (usuarioGuardado == usuarioLimpio && contrasenaGuardada == contrasena) {
            preferenciasCifradas.edit().putBoolean(claveSesionIniciada, true).apply()
            true
        } else {
            false
        }
    }

    fun cerrarSesion() {
        preferenciasCifradas.edit().putBoolean(claveSesionIniciada, false).apply()
    }
}