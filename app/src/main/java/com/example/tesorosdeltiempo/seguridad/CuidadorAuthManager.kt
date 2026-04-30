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

    // Validaciones para registro y login
    fun usuarioValido(nombreUsuario: String): Boolean {
        if (nombreUsuario.isBlank()) return false
        if (nombreUsuario.any { it.isWhitespace() }) return false
        return nombreUsuario.contains("@")
    }

    fun contrasenaSegura(contrasena: String): Boolean {
        if (contrasena.length < 8) return false
        if (contrasena.any { it.isWhitespace() }) return false
        val tieneMayuscula = contrasena.any { it.isUpperCase() }
        val tieneNumero = contrasena.any { it.isDigit() }
        val tieneSimbolo = contrasena.any { !it.isLetterOrDigit() }
        return tieneMayuscula && tieneNumero && tieneSimbolo
    }

    // Un solo cuidador registrado
    fun registrar(nombreUsuario: String, contrasena: String): Boolean {
        val usuarioLimpio = nombreUsuario.trim()
        if (!usuarioValido(usuarioLimpio)) return false
        if (!contrasenaSegura(contrasena)) return false

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
        if (!usuarioValido(usuarioLimpio)) return false
        if (contrasena.isBlank() || contrasena.any { it.isWhitespace() }) return false

        val usuarioGuardado = preferenciasCifradas.getString(claveUsuario, null) ?: return false
        val contrasenaGuardada = preferenciasCifradas.getString(claveContrasena, null) ?: return false

        return if (usuarioGuardado == usuarioLimpio && contrasenaGuardada == contrasena) {
            preferenciasCifradas.edit().putBoolean(claveSesionIniciada, true).apply()
            true
        } else {
            false
        }
    }

    // Apaga el flag pero no borra usuario guardado (así puede volver a entrar)
    fun cerrarSesion() {
        preferenciasCifradas.edit().putBoolean(claveSesionIniciada, false).apply()
    }

    // Borra el cuidador almacenado y apaga sesión
    fun eliminarUsuarioCuidador() {
        preferenciasCifradas.edit()
            .remove(claveUsuario)
            .remove(claveContrasena)
            .putBoolean(claveSesionIniciada, false)
            .apply()
    }
}