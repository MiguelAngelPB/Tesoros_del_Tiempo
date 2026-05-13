package com.example.tesorosdeltiempo.seguridad

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayInputStream

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

    // Lee toda la informacion descifrada del .enc
    fun leerArchivoCifradoABytes(contexto: Context, rutaArchivoCifrado: String): ByteArray {
        val ficheroEnDisco = File(rutaArchivoCifrado)
        val claveMaestra = MasterKey.Builder(contexto)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val archivoCifrado = EncryptedFile.Builder(
            contexto,
            ficheroEnDisco,
            claveMaestra,
            esquemaCifrado
        ).build()
        return archivoCifrado.openFileInput().use { it.readBytes() }
    }

    // Guarda los bytes en un nuevo .enc con la MasterKey del dispositivo
    fun guardarBytesComoArchivoCifrado(contexto: Context, bytes: ByteArray, prefijo: String): String {
        val nombre = "${prefijo}_${System.currentTimeMillis()}.enc"
        val destino = File(contexto.filesDir, nombre)
        val claveMaestra = MasterKey.Builder(contexto)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val archivoCifrado = EncryptedFile.Builder(
            contexto,
            destino,
            claveMaestra,
            esquemaCifrado
        ).build()
        archivoCifrado.openFileOutput().use { salida ->
            ByteArrayInputStream(bytes).use { it.copyTo(salida) }
        }
        return destino.absolutePath
    }
}