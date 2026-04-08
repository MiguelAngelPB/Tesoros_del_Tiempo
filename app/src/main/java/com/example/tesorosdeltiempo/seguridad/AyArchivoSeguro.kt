package com.example.tesorosdeltiempo.seguridad

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileOutputStream

// Helper para descifrar archivos guardados con EncryptedFile
object AyArchivoSeguro {

    private const val sufijoCifrado = ".enc"
    private val esquemaCifrado = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB

    fun esRutaArchivoCifrado(ruta: String?): Boolean {
        if (ruta.isNullOrBlank()) return false
        return ruta.endsWith(sufijoCifrado, ignoreCase = true)
    }

    // Descifra fichero temporal en cacheDir.
    fun descifrarAFicheroTemporal(
        contexto: Context,
        rutaArchivoCifrado: String,
        prefijoNombreTemporal: String,
        extensionTemporal: String
    ): File {
        val ficheroEnDisco = File(rutaArchivoCifrado)

        val claveMaestra = MasterKey.Builder(contexto)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val archivoCifradoJetpack = EncryptedFile.Builder(
            contexto,
            ficheroEnDisco,
            claveMaestra,
            esquemaCifrado
        ).build()

        val ficheroTemporal = File(
            contexto.cacheDir,
            "${prefijoNombreTemporal}_${System.currentTimeMillis()}${extensionTemporal}"
        )

        archivoCifradoJetpack.openFileInput().use { entrada ->
            FileOutputStream(ficheroTemporal).use { salida ->
                entrada.copyTo(salida)
            }
        }

        return ficheroTemporal
    }
}