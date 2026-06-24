package com.example.tesorosdeltiempo.seguridad

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.tesorosdeltiempo.R

// Desbloqueo al abrir la app usando huella, rostro o PIN/patrón del sistema (según el dispositivo)
object BiometricaDesbloqueoAy {

    private const val AUTENTICADORES =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun puedeDesbloquear(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTENTICADORES) ==
                BiometricManager.BIOMETRIC_SUCCESS

    fun solicitarAlAbrir(
        activity: FragmentActivity,
        alDesbloquear: () -> Unit,
        alCancelar: (() -> Unit)? = null,
        alSinMetodoDesbloqueo: () -> Unit
    ) {
        when (BiometricManager.from(activity).canAuthenticate(AUTENTICADORES)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                mostrarPrompt(activity, alDesbloquear, alCancelar)

            else -> alSinMetodoDesbloqueo()
        }
    }

    private fun mostrarPrompt(
        activity: FragmentActivity,
        alDesbloquear: () -> Unit,
        alCancelar: (() -> Unit)?
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    alDesbloquear()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> alCancelar?.invoke()
                    }
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.bio_titulo))
            .setSubtitle(activity.getString(R.string.bio_subtitulo))
            .setDescription(activity.getString(R.string.bio_descripcion))
            .setAllowedAuthenticators(AUTENTICADORES)
            .build()

        prompt.authenticate(info) // NOSONAR
    }
}